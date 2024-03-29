package com.developmentontheedge.be5.query.impl;

import com.developmentontheedge.be5.database.DbService;
import com.developmentontheedge.be5.exceptions.Be5Exception;
import com.developmentontheedge.be5.meta.Meta;
import com.developmentontheedge.be5.meta.UserAwareMeta;
import com.developmentontheedge.be5.metadata.DatabaseConstants;
import com.developmentontheedge.be5.metadata.RoleType;
import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.metadata.model.TableReference;
import com.developmentontheedge.be5.query.QueryConstants;
import com.developmentontheedge.be5.query.VarResolver;
import com.developmentontheedge.be5.query.impl.beautifiers.SubQueryBeautifier;
import com.developmentontheedge.be5.database.QRec;
import com.developmentontheedge.be5.database.adapters.QRecParser;
import com.developmentontheedge.be5.database.util.DynamicPropertyMeta;
import com.developmentontheedge.be5.query.util.Unzipper;
import com.developmentontheedge.be5.security.UserInfoProvider;
import com.developmentontheedge.be5.util.HashUrl;
import com.developmentontheedge.be5.util.MoreStrings;
import com.developmentontheedge.be5.util.Utils;
import com.developmentontheedge.beans.DynamicProperty;
import com.developmentontheedge.beans.DynamicPropertySet;
import com.developmentontheedge.beans.DynamicPropertySetAsMap;
import com.developmentontheedge.sql.format.ContextApplier;
import com.developmentontheedge.sql.model.AstBeSqlSubQuery;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import static com.developmentontheedge.be5.metadata.DatabaseConstants.ID_COLUMN_LABEL;
import static com.developmentontheedge.be5.query.QueryConstants.COL_ATTR_LINK;
import static com.developmentontheedge.be5.query.QueryConstants.COL_ATTR_URL;

public class CellFormatter
{
    private static final Logger log = Logger.getLogger(CellFormatter.class.getName());

    /**
     * The prefix constant for localized message.
     * <br/>This "{{{".
     */
    private static final String LOC_MSG_PREFIX = "{{{";

    /**
     * The postfix constant for localized message.
     * <br/>This "}}}".
     */
    private static final String LOC_MSG_POSTFIX = "}}}";

    private static final Pattern MESSAGE_PATTERN = MoreStrings.variablePattern(LOC_MSG_PREFIX, LOC_MSG_POSTFIX);
    private static final Unzipper subQueryUnzipper = Unzipper.on(Pattern.compile("<sql> SubQuery# [0-9]+</sql>")).trim();

    private final DbService db;
    private final UserAwareMeta userAwareMeta;
    private final Meta meta;
    private final UserInfoProvider userInfoProvider;
    private final Map<String, SubQueryBeautifier> subQueryBeautifiers;

    @Inject
    public CellFormatter(DbService db, UserAwareMeta userAwareMeta, Meta meta, UserInfoProvider userInfoProvider,
                         Map<String, SubQueryBeautifier> subQueryBeautifiers)
    {
        this.db = db;
        this.userAwareMeta = userAwareMeta;
        this.meta = meta;
        this.userInfoProvider = userInfoProvider;
        this.subQueryBeautifiers = subQueryBeautifiers;
    }

    private Object format(DynamicProperty cell, VarResolver varResolver, Query query, ContextApplier contextApplier)
    {
        String title = userAwareMeta.getColumnTitle(query.getEntity().getName(), query.getName(), cell.getName());
        cell.setDisplayName(title);

        Map<String, Map<String, String>> options = DynamicPropertyMeta.get(cell);
        Object content = getFormattedParts(cell, varResolver, query, contextApplier, options);

        if (content instanceof String)
        {
            content = getLocalizedCell(query.getEntity().getName(), query.getName(), (String) content);
        }

        Map<String, String> blankNullsProperties = options.get(QueryConstants.COL_ATTR_BLANKNULLS);
        if (blankNullsProperties != null)
        {
            if (content == null || content.equals("null"))
            {
                content = blankNullsProperties.getOrDefault("value", "");
            }
        }

        Map<String, String> nullIfProperties = options.get(QueryConstants.COL_ATTR_NULLIF);
        if (nullIfProperties != null)
        {
            if (content == null || content.equals(nullIfProperties.get("value")))
            {
                content = nullIfProperties.getOrDefault("result", "");
            }
        }

        Map<String, String> safeXmlProperties = options.get(QueryConstants.COL_ATTR_SAFEXML);
        if (safeXmlProperties != null)
        {
            if (content != null)
            {
                content = Utils.safeXML(content.toString());
            }
        }

        Map<String, String> linkProperties = options.get(COL_ATTR_LINK);
        if (linkProperties != null)
        {
            try
            {
                String url = generateUrl(linkProperties, varResolver);
                options.put(COL_ATTR_LINK, new HashMap<String, String>() {{
                        put(COL_ATTR_URL, url);
                        put("class", linkProperties.get("class"));
                }});
            }
            catch (Throwable e)
            {
                throw Be5Exception.internalInQuery(query,
                        new RuntimeException("Error in process COL_ATTR_LINK: " + cell.getName(), e));
            }
        }

        Map<String, String> refProperties = options.get(QueryConstants.COL_ATTR_REF);
        if (refProperties != null)
        {
            String url = generateRefUrl(refProperties, query, varResolver);
            options.put(COL_ATTR_LINK, new HashMap<String, String>() {{
                put(COL_ATTR_URL, url);
                put("class", refProperties.get("class"));
            }});
        }

        return content;
    }

    private String generateRefUrl(Map<String, String> refProperties, Query query, VarResolver varResolver)
    {
        String table = refProperties.get("table");
        HashUrl url = new HashUrl("table")
                .positional(table)
                .positional(refProperties.getOrDefault("queryName", DatabaseConstants.ALL_RECORDS_VIEW));
        for (TableReference reference : meta.getEntity(table).getAllReferences())
        {
            if (query.getEntity().getName().equals(reference.getTableTo()))
            {
                return url.named(reference.getName(), varResolver.resolve(ID_COLUMN_LABEL).toString()).toString();
            }
        }
        return url.toString();
    }

    private String generateUrl(Map<String, String> linkProperties, VarResolver varResolver)
    {
        if (linkProperties.containsKey(COL_ATTR_URL))
        {
            return varResolver.resolve(linkProperties.get(COL_ATTR_URL)).toString();
        }
        else
        {
            HashUrl url = new HashUrl("table").positional(linkProperties.get("table"))
                    .positional(linkProperties.getOrDefault("queryName", DatabaseConstants.ALL_RECORDS_VIEW));
            String cols = linkProperties.get("columns");
            String vals = linkProperties.get("using");
            if (cols != null && vals != null)
            {
                String[] colsArr = cols.split(",");
                String[] valuesArr = vals.split(",");

                Map<String, List<String>> mapOfList = new HashMap<>();
                for (int i = 0; i < colsArr.length; i++)
                {
                    Object resolveValue = varResolver.resolve(valuesArr[i]);
                    mapOfList.putIfAbsent(colsArr[i], new ArrayList<>());
                    mapOfList.get(colsArr[i]).add(resolveValue != null ? resolveValue.toString() : valuesArr[i]);
                }
                return url.named(mapOfList).toString();
            }
            return url.toString();
        }
    }

    private Object getFormattedParts(DynamicProperty cell, VarResolver varResolver, Query query,
                                     ContextApplier contextApplier, Map<String, Map<String, String>> options)
    {
        Objects.requireNonNull(cell);

        if (cell.getValue() == null)
        {
            return null;
        }

        if (cell.getValue() instanceof String)
        {
            StringBuilder builder = new StringBuilder();
            subQueryUnzipper.unzip((String) cell.getValue(), builder::append, subquery -> {
                builder.append(subQueryToString(subquery, varResolver, query, contextApplier));
                options.put("nosort", Collections.emptyMap());
            });
            return builder.toString();
        }
        else
        {
            return cell.getValue();
        }
    }

    private String subQueryToString(String subQueryName, VarResolver varResolver, Query query,
                                    ContextApplier contextApplier)
    {
        AstBeSqlSubQuery subQuery = contextApplier.getSubQuery(subQueryName, x -> {
            Object value = varResolver.resolve(x);
            return value != null ? value.toString() : null;
        });

        List<QRec> list = executeSubQuery(query, subQuery, varResolver);

        List<QRec> resultRows = new ArrayList<>();
        for (DynamicPropertySet dps : list)
        {
            resultRows.add(toRow(dps, varResolver, query, contextApplier));
        }

        String beautifierName = subQuery.getBeautifierName() != null ? subQuery.getBeautifierName() : "internal_glue";
        SubQueryBeautifier beautifier = subQueryBeautifiers.get(beautifierName);
        return beautifier.print(resultRows);
    }

    /**
     * Transforms a set of properties to a listDps. Each element of the listDps is a string or a table.
     */
    QRec toRow(DynamicPropertySet properties, VarResolver varResolver, Query query, ContextApplier contextApplier)
    {
        filterBeanWithRoles(properties, userInfoProvider.getCurrentRoles());
        addRowClass(properties);
        QRec resultCells = new QRec();
        DynamicPropertySet previousCells = new DynamicPropertySetAsMap();

        for (DynamicProperty cell : properties)
        {
            CompositeVarResolver cellVarResolver =
                    new CompositeVarResolver(new RootVarResolver(previousCells), varResolver);
            Object processedContent = format(cell, cellVarResolver, query, contextApplier);
            cell.setValue(processedContent);
            cell.setType(processedContent == null ? String.class : processedContent.getClass());
            previousCells.add(cell);
            if (!cell.isHidden())
            {
                resultCells.add(cell);
            }
        }

        return resultCells;
    }

    private void addRowClass(DynamicPropertySet properties)
    {
        DynamicProperty cssRowClassProperty = properties.getProperty(QueryConstants.CSS_ROW_CLASS);
        String cssRowClass = cssRowClassProperty != null ? (String) cssRowClassProperty.getValue() : null;
        if (cssRowClass != null && cssRowClass.length() > 0)
        {
            for (DynamicProperty property : properties)
            {
                Map<String, Map<String, String>> options = DynamicPropertyMeta.get(property);
                if (options.get("grouping") != null) continue;
                Map<String, String> css = options.putIfAbsent("css", new HashMap<>());
                if (css == null) css = options.get("css");

                String className = css.getOrDefault("class", "");
                css.put("class", className + " " + cssRowClass);
            }
        }
    }

    private static void filterBeanWithRoles(DynamicPropertySet dps, List<String> currentRoles)
    {
        for (Iterator<DynamicProperty> props = dps.propertyIterator(); props.hasNext();)
        {
            DynamicProperty prop = props.next();
            Map<String, String> info = DynamicPropertyMeta.get(prop).get(QueryConstants.COL_ATTR_ROLES);
            if (info == null)
            {
                continue;
            }

            String roles = info.get("name");
            List<String> roleList = Arrays.asList(roles.split(","));
            List<String> forbiddenRoles = new ArrayList<>();
            for (String userRole : roleList)
            {
                if (userRole.startsWith("!"))
                {
                    forbiddenRoles.add(userRole.substring(1));
                }
            }
            roleList.removeAll(forbiddenRoles);

            boolean hasAccess = isHasAccess(currentRoles, roleList, forbiddenRoles);
            if (!hasAccess)
            {
                prop.setHidden(true);
            }
        }
    }

    private static boolean isHasAccess(List<String> currentRoles, List<String> roleList, List<String> forbiddenRoles)
    {
        for (String role : roleList)
        {
            if (currentRoles.contains(role))
            {
                return true;
            }
        }
        if (!forbiddenRoles.isEmpty())
        {
            for (String currRole : currentRoles)
            {
                if (!forbiddenRoles.contains(currRole))
                {
                    return true;
                }
            }
        }
        return false;
    }

    private static class RootVarResolver implements VarResolver
    {
        private final DynamicPropertySet dps;

        RootVarResolver(DynamicPropertySet dps)
        {
            this.dps = dps;
        }

        @Override
        public Object resolve(String varName)
        {
            return dps.getValue(varName);
        }
    }

    private static class CompositeVarResolver implements VarResolver
    {
        private final VarResolver local;
        private final VarResolver parent;

        CompositeVarResolver(VarResolver local, VarResolver parent)
        {
            this.local = local;
            this.parent = parent;
        }

        @Override
        public Object resolve(String varName)
        {
            Object value = local.resolve(varName);

            if (value != null)
                return value;

            return parent.resolve(varName);
        }
    }

    private List<QRec> executeSubQuery(Query query, AstBeSqlSubQuery subQuery, VarResolver varResolver)
    {
        if (subQuery.getQuery() == null)
        {
            return Collections.emptyList();
        }

        String finalSql = subQuery.getQuery().toString();

        List<QRec> dynamicPropertySets;

        Object[] params = getParams(subQuery.getUsingParamNames(), varResolver);

        try
        {
            dynamicPropertySets = db.list(finalSql, new QRecParser(), params);
        }
        catch (Throwable e)
        {
            Be5Exception be5Exception = Be5Exception.internalInQuery(query, e);
            log.log(Level.SEVERE, be5Exception.toString() + " Final SQL: " + finalSql, be5Exception);

            QRec dynamicProperties = new QRec();
            dynamicProperties.add(new DynamicProperty(ID_COLUMN_LABEL, String.class, "-1"));
            dynamicProperties.add(new DynamicProperty("error", String.class,
                    userInfoProvider.getCurrentRoles()
                            .contains(RoleType.ROLE_SYSTEM_DEVELOPER) ? Be5Exception.getMessage(e) : "error"));
            dynamicPropertySets = Collections.singletonList(dynamicProperties);
        }

        if (dynamicPropertySets.size() == 0 && subQuery.getParameter("default") != null)
        {
            String value = userAwareMeta.getColumnTitle(query.getEntity().getName(), query.getName(),
                    subQuery.getParameter("default"));
            QRec dpsWithMessage = new QRec();
            dpsWithMessage.add(new DynamicProperty("message", String.class, value));
            return Collections.singletonList(dpsWithMessage);
        }
        else
        {
            return dynamicPropertySets;
        }
    }

    private Object[] getParams(String usingParamNames, VarResolver varResolver)
    {
        if (usingParamNames != null)
        {
            String[] paramNames = usingParamNames.split(",");
            Object[] params = new Object[paramNames.length];
            for (int i = 0; i < paramNames.length; i++)
            {
                params[i] = varResolver.resolve(paramNames[i]);
            }
            return params;
        }
        return new Object[]{};
    }

    private String getLocalizedCell(String entityName, String queryName, String content)
    {
        return MoreStrings.substituteVariables(content, MESSAGE_PATTERN, (message) ->
                userAwareMeta.getLocalization(entityName, queryName, message).orElse(message));
    }

}
