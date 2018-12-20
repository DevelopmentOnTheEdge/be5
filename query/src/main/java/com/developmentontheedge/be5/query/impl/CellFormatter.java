package com.developmentontheedge.be5.query.impl;

import com.developmentontheedge.be5.base.exceptions.Be5Exception;
import com.developmentontheedge.be5.base.services.Meta;
import com.developmentontheedge.be5.base.services.UserAwareMeta;
import com.developmentontheedge.be5.base.services.UserInfoProvider;
import com.developmentontheedge.be5.base.util.HashUrl;
import com.developmentontheedge.be5.base.util.MoreStrings;
import com.developmentontheedge.be5.database.DbService;
import com.developmentontheedge.be5.metadata.DatabaseConstants;
import com.developmentontheedge.be5.metadata.RoleType;
import com.developmentontheedge.be5.metadata.model.Entity;
import com.developmentontheedge.be5.metadata.model.EntityType;
import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.metadata.model.TableReference;
import com.developmentontheedge.be5.query.VarResolver;
import com.developmentontheedge.be5.query.model.RawCellModel;
import com.developmentontheedge.be5.query.services.QueryExecutor;
import com.developmentontheedge.be5.query.sql.DynamicPropertySetSimpleStringParser;
import com.developmentontheedge.be5.query.sql.TagsMapHandler;
import com.developmentontheedge.be5.query.util.Unzipper;
import com.developmentontheedge.beans.DynamicProperty;
import com.developmentontheedge.beans.DynamicPropertySet;
import com.developmentontheedge.beans.DynamicPropertySetAsMap;
import com.developmentontheedge.beans.DynamicPropertySetSupport;
import com.developmentontheedge.sql.format.ContextApplier;
import com.developmentontheedge.sql.model.AstBeSqlSubQuery;
import com.google.common.collect.ImmutableList;
import one.util.streamex.StreamEx;

import javax.inject.Inject;
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
import static com.developmentontheedge.be5.metadata.DatabaseConstants.SELECTION_VIEW;
import static java.util.Collections.emptyMap;

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
    private static final Unzipper unzipper = Unzipper.on(Pattern.compile("<sql> SubQuery# [0-9]+</sql>")).trim();

    private final DbService db;
    private final UserAwareMeta userAwareMeta;
    private final Meta meta;
    private final UserInfoProvider userInfoProvider;
    private final QueryExecutor queryService;

    @Inject
    public CellFormatter(DbService db, UserAwareMeta userAwareMeta, Meta meta, UserInfoProvider userInfoProvider, QueryExecutor queryService)
    {
        this.db = db;
        this.userAwareMeta = userAwareMeta;
        this.meta = meta;
        this.userInfoProvider = userInfoProvider;
        this.queryService = queryService;
    }

    /**
     * Executes subqueries of the cell or returns the cell content itself.
     */
    Object formatCell(RawCellModel cell, DynamicPropertySet previousCells, Query query, ContextApplier contextApplier)
    {
        return format(cell, new RootVarResolver(previousCells), query, contextApplier);
    }

    private Object format(RawCellModel cell, VarResolver varResolver, Query query, ContextApplier contextApplier)
    {
        //ImmutableList<Object> formattedParts = getFormattedPartsWithoutLink(cell, varResolver);

        Object formattedContent = getFormattedPartsWithoutLink(cell, varResolver, query, contextApplier);
//        if(formattedContent == null) {
//            return null;
//        }
        //formattedContent = StreamEx.of(formattedParts).map(this::print).joining();
        if (formattedContent instanceof String)
        {
            formattedContent = getLocalizedCell(query.getEntity().getName(), query.getName(), (String) formattedContent);
        }
        //TODO && extraQuery == Be5QueryExecutor.ExtraQuery.DEFAULT

        Map<String, String> blankNullsProperties = cell.options.get(DatabaseConstants.COL_ATTR_BLANKNULLS);
        if (blankNullsProperties != null)
        {
            if (formattedContent == null || formattedContent.equals("null"))
            {
                formattedContent = blankNullsProperties.getOrDefault("value", "");
            }
        }


        Map<String, String> nullIfProperties = cell.options.get(DatabaseConstants.COL_ATTR_NULLIF);
        if (nullIfProperties != null)
        {
            if (formattedContent == null || formattedContent.equals(nullIfProperties.get("value")))
            {
                formattedContent = nullIfProperties.getOrDefault("result", "");
            }
        }

        Map<String, String> linkProperties = cell.options.get(DatabaseConstants.COL_ATTR_LINK);
        if (linkProperties != null)
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
                cell.options.put(DatabaseConstants.COL_ATTR_LINK, new HashMap<String, String>() {{
                        put("url", utlStr);
                        put("class", linkProperties.get("class"));
                }});
            }
            catch (Throwable e)
            {
                throw Be5Exception.internalInQuery(query,
                        new RuntimeException("Error in process COL_ATTR_LINK: " + cell.name, e));
            }
        }

        Map<String, String> refProperties = cell.options.get(DatabaseConstants.COL_ATTR_REF);
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
            cell.options.put(DatabaseConstants.COL_ATTR_LINK, new HashMap<String, String>() {{
                put("url", utlStr);
                put("class", refProperties.get("class"));
            }});
        }

        return formattedContent;
    }

    private Object getFormattedPartsWithoutLink(RawCellModel cell, VarResolver varResolver, Query query, ContextApplier contextApplier)
    {
        Objects.requireNonNull(cell);

        boolean hasLink = cell.options.containsKey("link");
        Map<String, String> link = null;
        if (hasLink)
        {
            link = cell.options.get("link");
            cell.options.remove("link");
        }

        ImmutableList.Builder<Object> builder = ImmutableList.builder();

        if (cell.content == null)
        {
            return null;
        }

        Object content;
        if (cell.content instanceof String)
        {
            unzipper.unzip((String) cell.content, builder::add, subquery ->
                    builder.add(toTable(subquery, varResolver, query, contextApplier))
            );
            ImmutableList<Object> formattedParts = builder.build();
            content = StreamEx.of(formattedParts).map(this::print).joining();
        }
        else
        {
            content = cell.content;
        }

        if (hasLink)
        {
            cell.options.put("link", link);
        }
        return content;
    }

    /**
     * Dynamically casts tables to string using default formatting;
     */
    private String print(Object formattedPart)
    {
        if (formattedPart instanceof String)
        {
            return (String) formattedPart;
        }
        else if (formattedPart instanceof List)
        {
            @SuppressWarnings("unchecked")
            List<List<Object>> table = (List<List<Object>>) formattedPart;
            //todo support beautifiers - <br/> or ; or ...
            return StreamEx.of(table).map(list -> StreamEx.of(list).map(this::print).joining(" ")).joining("<br/> ");
//            return StreamEx.of(table).map(list -> StreamEx.of(list).map(this::print).joining(" "))
//                    .map(x -> "<div class=\"inner-sql-row\">" + x + "</div>").joining("");
        }
        else
        {
            throw new AssertionError(formattedPart.getClass().getName());
        }
    }

    /**
     * Returns a two-dimensional listDps of processed content. Each element is either a string or a table.
     */
    private List<List<Object>> toTable(String subQueryName, VarResolver varResolver, Query query, ContextApplier contextApplier)
    {
        List<DynamicPropertySet> list = executeSubQuery(subQueryName, varResolver, query, contextApplier);

        List<List<Object>> lists = new ArrayList<>();

        for (DynamicPropertySet dps : list)
        {
            List<Object> objects = toRow(dps, varResolver, query, contextApplier);
            lists.add(objects);
        }

        return lists;
    }

    /**
     * Transforms a set of properties to a listDps. Each element of the listDps is a string or a table.
     */
    private List<Object> toRow(DynamicPropertySet dps, VarResolver varResolver, Query query, ContextApplier contextApplier)
    {
        DynamicPropertySet previousCells = new DynamicPropertySetAsMap();

        return StreamEx.of(dps.spliterator()).map(property -> {
            String name = property.getName();
            Object value = property.getValue();
            RawCellModel rawCellModel = new RawCellModel(value != null ? value.toString() : "");
            CompositeVarResolver compositeVarResolver = new CompositeVarResolver(new RootVarResolver(previousCells), varResolver);
            Object processedCell = format(rawCellModel, compositeVarResolver, query, contextApplier);
            previousCells.add(new DynamicProperty(name, String.class, processedCell));
            return !name.startsWith("___") ? processedCell : "";
        }).toList();
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

    private List<DynamicPropertySet> executeSubQuery(String subQueryName, VarResolver varResolver,
                                                     Query query, ContextApplier contextApplier)
    {
        AstBeSqlSubQuery subQuery = contextApplier.getSubQuery(subQueryName, x -> {
            Object value = varResolver.resolve(x);
            return value != null ? value.toString() : null;
        });

        if (subQuery.getQuery() == null)
        {
            return Collections.emptyList();
        }

        String finalSql = subQuery.getQuery().toString();

        List<DynamicPropertySet> dynamicPropertySets;

        Object[] params;
        String usingParamNames = subQuery.getUsingParamNames();
        if (usingParamNames != null)
        {
            String[] paramNames = usingParamNames.split(",");
            params = new Object[paramNames.length];
            for (int i = 0; i < paramNames.length; i++)
            {
                params[i] = varResolver.resolve(paramNames[i]);
            }
        }
        else
        {
            params = new Object[]{};
        }

        try
        {
            dynamicPropertySets = db.list(finalSql, new DynamicPropertySetSimpleStringParser(), params);
        }
        catch (Throwable e)
        {
            Be5Exception be5Exception = Be5Exception.internalInQuery(query, e);
            log.log(Level.SEVERE, be5Exception.toString() + " Final SQL: " + finalSql, be5Exception);

            DynamicPropertySetSupport dynamicProperties = new DynamicPropertySetSupport();
            dynamicProperties.add(new DynamicProperty("___ID", String.class, "-1"));
            dynamicProperties.add(new DynamicProperty("error", String.class,
                    userInfoProvider.getCurrentRoles().contains(RoleType.ROLE_SYSTEM_DEVELOPER) ? Be5Exception.getMessage(e) : "error"));
            dynamicPropertySets = Collections.singletonList(dynamicProperties);
        }

        if (dynamicPropertySets.size() == 0 && subQuery.getParameter("default") != null)
        {
            String value = userAwareMeta.getColumnTitle(query.getEntity().getName(), query.getName(),
                    subQuery.getParameter("default"));
            DynamicPropertySetSupport dpsWithMessage = new DynamicPropertySetSupport();
            dpsWithMessage.add(new DynamicProperty("message", String.class, value));
            return Collections.singletonList(dpsWithMessage);
        }
        else
        {
            return dynamicPropertySets;
        }
    }

    /**
     * Returns a localized title of an operation in user's preferred language.
     */
    public String getLocalizedCell(String entityName, String queryName, String content)
    {
        String localized = MoreStrings.substituteVariables(content, MESSAGE_PATTERN, (message) ->
                userAwareMeta.getLocalization(entityName, queryName, message)
                        .orElseGet(() -> localizeDictionaryValues(entityName, message)
                                .orElse(message))
        );

        if (localized.startsWith("{{{") && localized.endsWith("}}}"))
        {
            String clearContent = localized.substring(3, localized.length() - 3);
            return userAwareMeta.getLocalization(entityName, queryName, clearContent)
                    .orElse(clearContent);
        }

        return localized;
    }

    private Optional<String> localizeDictionaryValues(String entityName, String key)
    {
        for (TableReference reference : meta.getEntity(entityName).getAllReferences())
        {
            String tableTo = reference.getTableTo();
            Entity entity = tableTo != null ? meta.getEntity(tableTo) : null;
            if (entity != null && entity.getType() == EntityType.DICTIONARY)
            {
                Map<String, String> tags = queryService.build(meta.getQuery(entity.getName(), SELECTION_VIEW), emptyMap())
                        .query(new TagsMapHandler());
                String value = tags.get(key);
                if (value != null)
                {
                    return Optional.of(value);
                }
            }
        }
        return Optional.empty();
    }
}
