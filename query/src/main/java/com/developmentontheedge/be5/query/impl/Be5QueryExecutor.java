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
import com.developmentontheedge.be5.query.impl.utils.QueryUtils;
import com.developmentontheedge.be5.query.sql.DpsRecordAdapter;
import com.developmentontheedge.be5.query.sql.DynamicPropertySetSimpleStringParser;
import com.developmentontheedge.beans.DynamicProperty;
import com.developmentontheedge.beans.DynamicPropertySet;
import com.developmentontheedge.beans.DynamicPropertySetSupport;
import com.developmentontheedge.sql.format.ContextApplier;
import com.developmentontheedge.sql.format.LimitsApplier;
import com.developmentontheedge.sql.format.MacroExpander;
import com.developmentontheedge.sql.format.QueryContext;
import com.developmentontheedge.sql.format.Simplifier;
import com.developmentontheedge.sql.model.AstBeSqlSubQuery;
import com.developmentontheedge.sql.model.AstStart;
import com.developmentontheedge.sql.model.SqlQuery;
import one.util.streamex.StreamEx;

import java.sql.PreparedStatement;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;


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
            return meta.getQuery(entityName == null ? query.getEntity().getName() : entityName, queryName)
                       .getFinalQuery();
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
            return getColumnNames(getFinalSql().getQuery().toString());
        throw new UnsupportedOperationException("Query type " + query.getType() + " is not supported yet");
    }

    @Override
    public AstStart getFinalSql()
    {
        String queryText = query.getFinalQuery();
        if (queryText.isEmpty()) return null;

        AstStart ast = parseQuery(queryText);
        new MacroExpander().expandMacros(ast);

        QueryUtils.resolveTypeOfRefColumn(ast, query.getEntity().getName(), meta);
        QueryUtils.applyFilters(ast, query.getEntity().getName(), parameters, meta);
        applyCategory(ast);

        contextApplier.applyContext(ast);

        if (selectable) QueryUtils.addIDColumnLabel(ast, query);

        if (executeType == ExecuteType.COUNT)
        {
            QueryUtils.countFromQuery(ast.getQuery());
        }

        if (executeType == ExecuteType.DEFAULT)
        {
            QueryUtils.applySort(ast, getSchema(ast), orderColumn + (selectable ? -1 : 0), orderDir);
            new LimitsApplier(offset, limit).transform(ast);
        }

        Simplifier.simplify(ast);
        return ast;
    }

    private AstStart parseQuery(String queryText)
    {
        try
        {
            return SqlQuery.parse(queryText);
        }
        catch (RuntimeException e)
        {
            log.log(Level.SEVERE, "SqlQuery.parse error: ", e);
            throw Be5Exception.internalInQuery(query, e);
        }
    }

    private DynamicProperty[] getSchema(AstStart ast)
    {
        return db.execute(conn -> {
            String sql = db.format(ast);
            try (PreparedStatement ps = conn.prepareStatement(sql))
            {
                return DpsRecordAdapter.createSchema(ps.getMetaData());
            }
            catch (SQLException e)
            {
                throw Be5Exception.internal(sql, e);
            }
        });
    }

    private void applyCategory(AstStart ast)
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
