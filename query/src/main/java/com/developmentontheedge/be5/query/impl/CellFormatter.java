package com.developmentontheedge.be5.query.impl;

import com.developmentontheedge.be5.database.DbService;
import com.developmentontheedge.be5.exceptions.Be5Exception;
import com.developmentontheedge.be5.meta.Meta;
import com.developmentontheedge.be5.meta.UserAwareMeta;
import com.developmentontheedge.be5.metadata.DatabaseConstants;
import com.developmentontheedge.be5.metadata.RoleType;
import com.developmentontheedge.be5.metadata.model.Entity;
import com.developmentontheedge.be5.metadata.model.EntityType;
import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.metadata.model.TableReference;
import com.developmentontheedge.be5.query.QueryConstants;
import com.developmentontheedge.be5.query.VarResolver;
import com.developmentontheedge.be5.query.impl.beautifiers.SubQueryBeautifier;
import com.developmentontheedge.be5.query.model.beans.QRec;
import com.developmentontheedge.be5.query.services.QueriesService;
import com.developmentontheedge.be5.query.sql.QRecParser;
import com.developmentontheedge.be5.query.util.DynamicPropertyMeta;
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
import one.util.streamex.StreamEx;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import static com.developmentontheedge.be5.metadata.DatabaseConstants.ID_COLUMN_LABEL;
import static com.developmentontheedge.be5.query.QueryConstants.COL_ATTR_LINK;
import static com.developmentontheedge.be5.query.QueryConstants.COL_ATTR_URL;
import static java.util.Collections.singletonMap;

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
    private final Provider<QueriesService> queries;
    private final Map<String, SubQueryBeautifier> subQueryBeautifiers;

    @Inject
    public CellFormatter(DbService db, UserAwareMeta userAwareMeta, Meta meta, UserInfoProvider userInfoProvider,
                         Provider<QueriesService> queries, Map<String, SubQueryBeautifier> subQueryBeautifiers)
    {
        this.db = db;
        this.userAwareMeta = userAwareMeta;
        this.meta = meta;
        this.userInfoProvider = userInfoProvider;
        this.queries = queries;
        this.subQueryBeautifiers = subQueryBeautifiers;
    }

    /**
     * Executes subqueries of the cell or returns the cell content itself.
     */
    Object formatCell(DynamicProperty cell, DynamicPropertySet previousCells, Query query, ContextApplier contextApplier)
    {
        return format(cell, new RootVarResolver(previousCells), query, contextApplier);
    }

    private Object format(DynamicProperty cell, VarResolver varResolver, Query query, ContextApplier contextApplier)
    {
        String title = userAwareMeta.getColumnTitle(query.getEntity().getName(), query.getName(), cell.getName());
        cell.setDisplayName(title);

        Map<String, Map<String, String>> options = DynamicPropertyMeta.get(cell);
        Object formattedContent = getFormattedParts(cell, varResolver, query, contextApplier, options);

        if (formattedContent instanceof String)
        {
            formattedContent = getLocalizedCell(query.getEntity().getName(), query.getName(), (String) formattedContent);
        }

        Map<String, String> blankNullsProperties = options.get(QueryConstants.COL_ATTR_BLANKNULLS);
        if (blankNullsProperties != null)
        {
            if (formattedContent == null || formattedContent.equals("null"))
            {
                formattedContent = blankNullsProperties.getOrDefault("value", "");
            }
        }

        Map<String, String> nullIfProperties = options.get(QueryConstants.COL_ATTR_NULLIF);
        if (nullIfProperties != null)
        {
            if (formattedContent == null || formattedContent.equals(nullIfProperties.get("value")))
            {
                formattedContent = nullIfProperties.getOrDefault("result", "");
            }
        }

        Map<String, String> safeXmlProperties = options.get(QueryConstants.COL_ATTR_SAFEXML);
        if (safeXmlProperties != null)
        {
            if (formattedContent != null)
            {
                formattedContent = Utils.safeXML(formattedContent.toString());
            }
        }

        Map<String, String> linkProperties = options.get(COL_ATTR_LINK);
        if (linkProperties != null && !linkProperties.containsKey(COL_ATTR_URL))
        {
            try
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
                    url = url.named(mapOfList);
                }
                String utlStr = url.toString();
                options.put(COL_ATTR_LINK, new HashMap<String, String>() {{
                        put(COL_ATTR_URL, utlStr);
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
            String table = refProperties.get("table");
            HashUrl url = new HashUrl("table")
                    .positional(table)
                    .positional(refProperties.getOrDefault("queryName", DatabaseConstants.ALL_RECORDS_VIEW));
            for (TableReference reference : meta.getEntity(table).getAllReferences())
            {
                if (query.getEntity().getName().equals(reference.getTableTo()))
                {
                    url = url.named(reference.getName(), varResolver.resolve(ID_COLUMN_LABEL).toString());
                    break;
                }
            }
            String utlStr = url.toString();
            options.put(COL_ATTR_LINK, new HashMap<String, String>() {{
                put(COL_ATTR_URL, utlStr);
                put("class", refProperties.get("class"));
            }});
        }

        return formattedContent;
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

        List<Map<String, Object>> lists = new ArrayList<>();
        for (DynamicPropertySet dps : list)
        {
            Map<String, Object> values = toRow(dps, varResolver, query, contextApplier);
            lists.add(values);
        }

        String beautifierName = subQuery.getBeautifierName() != null ? subQuery.getBeautifierName() : "internal_glue";
        SubQueryBeautifier beautifier = subQueryBeautifiers.get(beautifierName);
        return beautifier.print(lists);
    }

    /**
     * Transforms a set of properties to a listDps. Each element of the listDps is a string or a table.
     */
    private Map<String, Object> toRow(DynamicPropertySet dps, VarResolver varResolver, Query query,
                                      ContextApplier contextApplier)
    {
        DynamicPropertySet previousCells = new DynamicPropertySetAsMap();

        return StreamEx.of(dps.spliterator())
                .collect(Utils.toLinkedMap(DynamicProperty::getName, property ->
        {
            String name = property.getName();
            Object value = property.getValue();
            //RawCellModel rawCellModel = new RawCellModel(value != null ? value.toString() : "");
            if (value == null) property.setValue("");
            CompositeVarResolver compositeVarResolver =
                    new CompositeVarResolver(new RootVarResolver(previousCells), varResolver);
            Object processedCell = format(property, compositeVarResolver, query, contextApplier);
            previousCells.add(new DynamicProperty(name, String.class, processedCell));
            return !name.startsWith("___") ? processedCell : "";
        }));
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

    /**
     * Returns a localized title of an operation in user's preferred language.
     */
    public String getLocalizedCell(String entityName, String queryName, String content)
    {
        return MoreStrings.substituteVariables(content, MESSAGE_PATTERN, (message) ->
                userAwareMeta.getLocalization(entityName, queryName, message)
                        .orElseGet(() -> localizeDictionaryValues(entityName, message)
                                .orElse(message))
        );
    }

    private Optional<String> localizeDictionaryValues(String entityName, String key)
    {
        if (!meta.getProject().getEntityNames().contains(entityName)) return Optional.empty();

        for (TableReference reference : meta.getEntity(entityName).getAllReferences())
        {
            String tableTo = reference.getTableTo();
            Entity entity = tableTo != null ? meta.getEntity(tableTo) : null;
            if (entity != null && entity.getType() == EntityType.DICTIONARY)
            {
                String primaryKey = entity.getPrimaryKey();
                String[][] tags = queries.get().getTagsFromSelectionView(entity.getName(), singletonMap(primaryKey, key));
                if (tags.length > 0)
                {
                    return Optional.of(tags[0][1]);
                }
            }
        }
        return Optional.empty();
    }
}
