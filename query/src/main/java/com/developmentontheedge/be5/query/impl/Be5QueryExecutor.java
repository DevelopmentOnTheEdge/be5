package com.developmentontheedge.be5.query.impl;

import com.developmentontheedge.be5.base.FrontendConstants;
import com.developmentontheedge.be5.base.exceptions.Be5Exception;
import com.developmentontheedge.be5.base.model.UserInfo;
import com.developmentontheedge.be5.base.services.Meta;
import com.developmentontheedge.be5.base.services.UserAwareMeta;
import com.developmentontheedge.be5.database.DbService;
import com.developmentontheedge.be5.database.sql.ResultSetParser;
import com.developmentontheedge.be5.metadata.QueryType;
import com.developmentontheedge.be5.metadata.RoleType;
import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.query.QuerySession;
import com.developmentontheedge.be5.query.VarResolver;
import com.developmentontheedge.be5.query.impl.utils.CategoryFilter;
import com.developmentontheedge.be5.query.impl.utils.DebugQueryLogger;
import com.developmentontheedge.be5.query.sql.DpsRecordAdapter;
import com.developmentontheedge.be5.query.sql.DynamicPropertySetSimpleStringParser;
import com.developmentontheedge.beans.DynamicProperty;
import com.developmentontheedge.beans.DynamicPropertySet;
import com.developmentontheedge.beans.DynamicPropertySetSupport;
import com.developmentontheedge.sql.format.ContextApplier;
import com.developmentontheedge.sql.format.LimitsApplier;
import com.developmentontheedge.sql.format.QueryContext;
import com.developmentontheedge.sql.format.Simplifier;
import com.developmentontheedge.sql.model.AstBeSqlSubQuery;
import com.developmentontheedge.sql.model.AstStart;
import com.developmentontheedge.sql.model.SqlQuery;
import one.util.streamex.StreamEx;

import java.sql.PreparedStatement;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static com.developmentontheedge.be5.query.impl.utils.QueryUtils.addIDColumnIfNeeded;
import static com.developmentontheedge.be5.query.impl.utils.QueryUtils.applyFilters;
import static com.developmentontheedge.be5.query.impl.utils.QueryUtils.applySort;
import static com.developmentontheedge.be5.query.impl.utils.QueryUtils.countFromQuery;
import static com.developmentontheedge.be5.query.impl.utils.QueryUtils.resolveTypeOfRefColumn;


/**
 * A modern query executor that uses our new parser.
 */
public class Be5QueryExecutor extends AbstractQueryExecutor
{
    private static final Logger log = Logger.getLogger(Be5QueryExecutor.class.getName());

    private enum ExecuteType
    {
        DEFAULT, COUNT, AGGREGATE
    }

    //todo move to separate file
    private final class ExecutorQueryContext implements QueryContext
    {
        private final Map<String, AstBeSqlSubQuery> subQueries = new HashMap<>();

        @Override
        public Map<String, AstBeSqlSubQuery> getSubQueries()
        {
            return subQueries;
        }

        @Override
        public StreamEx<String> roles()
        {
            return StreamEx.of(userInfo.getCurrentRoles());
        }

        @Override
        public String resolveQuery(String entityName, String queryName)
        {
            return meta.getQueryCode(entityName == null ? query.getEntity().getName() : entityName, queryName);
        }

        @Override
        public String getUserName()
        {
            return userInfo.getUserName();
        }

        @Override
        public Object getSessionVariable(String name)
        {
            return querySession.get(name);
        }

        @Override
        public String getParameter(String name)
        {
            if (parameters.get(name) == null)
                return null;
            if (parameters.get(name).size() != 1)
                throw new IllegalStateException(name + " contains more than one value");
            else
                return parameters.get(name).get(0) + "";
        }

        @Override
        public List<String> getListParameter(String name)
        {
            if (parameters.get(name) == null) return null;
            return parameters.get(name).stream().map(x -> x + "").collect(Collectors.toList());
        }

        @Override
        public Map<String, String> asMap()
        {
            return StreamEx.ofKeys(parameters).toMap(this::getParameter);
        }

        @Override
        public String getDictionaryValue(String tagName, String name, Map<String, String> conditions)
        {
            throw new RuntimeException("todo");
//            EntityModel entityModel = database.get().getEntity(tagName);
//            RecordModel row = entityModel.getBy(conditions);
//
//            String value = row.getValue(name).toString();
//
//            if(!meta.isNumericColumn(entityModel.getEntity(), name))
//            {
//                value = "'" + value + "'";
//            }
//
//            return value;
        }
    }

    private final Meta meta;
    private final DbService db;
    private final UserAwareMeta userAwareMeta;

    private final Map<String, List<Object>> parameters;
    private final UserInfo userInfo;
    private final QuerySession querySession;

    private ExecutorQueryContext executorQueryContext;
    private ContextApplier contextApplier;
    private ExecuteType executeType;


    public Be5QueryExecutor(Query query, Map<String, List<Object>> parameters, UserInfo userInfo,
                            QuerySession querySession, Meta meta, UserAwareMeta userAwareMeta, DbService db)
    {
        super(query);

        this.parameters = parameters;
        this.userInfo = userInfo;
        this.querySession = querySession;

        this.meta = meta;
        this.userAwareMeta = userAwareMeta;
        this.db = db;

        this.executorQueryContext = new ExecutorQueryContext();
        this.contextApplier = new ContextApplier(executorQueryContext);
        this.executeType = ExecuteType.DEFAULT;

        selectable = !query.getOperationNames().isEmpty() && query.getType() == QueryType.D1;
    }

    @Override
    public <T> List<T> execute(ResultSetParser<T> parser)
    {
        if (query.getType().equals(QueryType.D1) || query.getType().equals(QueryType.D1_UNKNOWN))
        {
            try
            {
                return db.list(getFinalSql(), parser);
            }
            catch (RuntimeException e)
            {
                throw Be5Exception.internalInQuery(query, e);
            }
        }

        throw new UnsupportedOperationException("Query type " + query.getType() + " is not supported yet");
    }

    @Override
    public <T> T getRow(ResultSetParser<T> parser)
    {
        List<T> list = execute(parser);
        if (list.size() == 0)
        {
            return null;
        }
        else
        {
            return list.get(0);
        }
    }

    @Override
    public List<String> getColumnNames() throws Be5Exception
    {
        if (query.getType().equals(QueryType.D1) || query.getType().equals(QueryType.D1_UNKNOWN))
            return getColumnNames(getFinalSql());
        throw new UnsupportedOperationException("Query type " + query.getType() + " is not supported yet");
    }

    @Override
    public String getFinalSql()
    {
        DebugQueryLogger dql = new DebugQueryLogger();
        dql.log("Orig", query.getQuery());

        String queryText = meta.getQueryCode(query);

        dql.log("After FreeMarker", queryText);
        if (queryText.isEmpty())
            return "";
        AstStart ast;
        try
        {
            ast = SqlQuery.parse(queryText);
        }
        catch (RuntimeException e)
        {
            log.log(Level.SEVERE, "SqlQuery.parse error: ", e);
            throw Be5Exception.internalInQuery(query, e);

            //ast = SqlQuery.parse("select 'error'");
        }
        dql.log("Compiled", ast);

        resolveTypeOfRefColumn(ast, meta);

        // FILTERS
        applyFilters(ast, query.getEntity().getName(), parameters, meta);

        // CATEGORY
        applyCategory(dql, ast);

        // CONTEXT
        contextApplier.applyContext(ast);
        dql.log("With context", ast);

        // ID COLUMN
        addIDColumnIfNeeded(ast, query, dql);

        // SIMPLIFY
        Simplifier.simplify(ast);
        dql.log("Simplified", ast);

        if (executeType == ExecuteType.COUNT)
        {
            countFromQuery(ast.getQuery());
            dql.log("Count(1) from query", ast);
        }

        if (executeType == ExecuteType.DEFAULT)
        {
            // SORT ORDER
            applySort(ast, getSchema(ast.getQuery().toString()), dql, orderColumn + (selectable ? -1 : 0), orderDir);

            // LIMITS
            new LimitsApplier(offset, limit).transform(ast);
            dql.log("With limits", ast);
        }

        return ast.getQuery().toString();
    }

    private DynamicProperty[] getSchema(String sql)
    {
        return db.execute(conn -> {
            try (PreparedStatement ps = conn.prepareStatement(db.format(sql)))
            {
                return DpsRecordAdapter.createSchema(ps.getMetaData());
            }
        });
    }

    private void applyCategory(DebugQueryLogger dql, AstStart ast)
    {
        String categoryString = executorQueryContext.getParameter(FrontendConstants.CATEGORY_ID_PARAM);
        if (categoryString != null)
        {
            long categoryId;
            try
            {
                categoryId = Long.parseLong(categoryString);
            }
            catch (NumberFormatException e)
            {
                throw Be5Exception.internalInQuery(query,
                        new IllegalArgumentException("Invalid category: " + categoryString, e));
            }

            new CategoryFilter(query.getEntity().getName(), query.getEntity().getPrimaryKey(), categoryId).apply(ast);
            dql.log("With category", ast);
        }
    }

    private List<String> getColumnNames(String sql)
    {
        return db.select(sql, rs -> {
            List<String> result = new ArrayList<>();
            ResultSetMetaData meta = rs.getMetaData();

            for (int column = 1, count = meta.getColumnCount(); column <= count; column++)
            {
                result.add(meta.getColumnName(column));
            }

            return result;
        });
    }

//    private StreamEx<DynamicPropertySet> streamCustomQuery()
//    {
//        try
//        {
//            QueryIterator iterator = Classes.tryLoad( query.getQueryCompiled().validate(), QueryIterator.class )
//                    .getConstructor( UserInfo.class, ParamHelper.class, DbmsConnector.class, long.class, long.class )
//                    // TODO: create and pass ParamHelper
//                    .newInstance( userInfo.getUserInfo(), new MapParamHelper(parameters), connector, offset, limit );
//
//            if (iterator instanceof Be5Query)
//            {
//                ((Be5Query) iterator).initialize(injector);
//            }
//
//            @SuppressWarnings("unchecked")
//            StreamEx<DynamicPropertySet> streamDps = StreamEx.of( iterator );
//            return streamDps;
//        }
//        catch( InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
//                | NoSuchMethodException | SecurityException | ProjectElementException e )
//        {
//            throw Be5Exception.internalInQuery( e, query );
//        }
//    }

//    private void processMeta(Object value, Map<String, Map<String, String>> meta)
//    {
//        if (subQueryKeys.contains(value) && !meta.containsKey("sql"))
//        {
//            AstBeSqlSubQuery subQuery = contextApplier.getSubQuery((String) value, s -> "");
//            meta.put("sql", StreamEx.of("beautifier", "default").mapToEntry(subQuery::getParameter).nonNullValues().toSortedMap());
//        }
//    }

    @Override
    public List<DynamicPropertySet> execute()
    {
        executeType = ExecuteType.DEFAULT;
        return execute(new DynamicPropertySetSimpleStringParser());
    }

    @Override
    public List<DynamicPropertySet> executeAggregate()
    {
        executeType = ExecuteType.AGGREGATE;
        return execute(new DynamicPropertySetSimpleStringParser());
    }

    @Override
    public long count()
    {
        executeType = ExecuteType.COUNT;
        return (Long) execute(new DynamicPropertySetSimpleStringParser()).get(0).asMap().get("count");
    }

    @Override
    public DynamicPropertySet getRow()
    {
        return getRow(new DynamicPropertySetSimpleStringParser());
    }

    @Override
    public List<DynamicPropertySet> executeSubQuery(String subqueryName, VarResolver varResolver)
    {
        AstBeSqlSubQuery subQuery = contextApplier.getSubQuery(subqueryName, x -> {
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
            params = new Object[]{paramNames.length};
            for (int i = 0; i < paramNames.length; i++)
            {
                params[i] = varResolver.resolve(paramNames[i]).toString();
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
            //TODO only for Document presentation, for operations must be error throw
            Be5Exception be5Exception = Be5Exception.internalInQuery(query, e);
            log.log(Level.SEVERE, be5Exception.toString() + " Final SQL: " + finalSql, be5Exception);

            DynamicPropertySetSupport dynamicProperties = new DynamicPropertySetSupport();
            dynamicProperties.add(new DynamicProperty("___ID", String.class, "-1"));
            dynamicProperties.add(new DynamicProperty("error", String.class,
                    userInfo.getCurrentRoles().contains(RoleType.ROLE_SYSTEM_DEVELOPER) ? Be5Exception.getMessage(e) : "error"));
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

}
