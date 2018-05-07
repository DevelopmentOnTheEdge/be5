package com.developmentontheedge.be5.query.impl;

import com.developmentontheedge.be5.api.services.ConnectionService;
import com.developmentontheedge.be5.api.sql.DpsRecordAdapter;
import com.developmentontheedge.be5.api.services.Meta;
import com.developmentontheedge.be5.databasemodel.EntityModel;
import com.developmentontheedge.be5.databasemodel.RecordModel;
import com.developmentontheedge.be5.databasemodel.impl.DatabaseModel;
import com.developmentontheedge.be5.exceptions.Be5Exception;
import com.developmentontheedge.be5.api.helpers.UserInfoHolder;
import com.developmentontheedge.be5.api.services.DatabaseService;
import com.developmentontheedge.be5.api.services.SqlService;
import com.developmentontheedge.be5.api.sql.ResultSetParser;
import com.developmentontheedge.be5.metadata.QueryType;
import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.query.impl.utils.CategoryFilter;
import com.developmentontheedge.be5.query.impl.utils.DebugQueryLogger;
import com.developmentontheedge.beans.DynamicProperty;
import com.developmentontheedge.beans.DynamicPropertySet;
import com.developmentontheedge.beans.DynamicPropertySetSupport;
import com.developmentontheedge.sql.format.Context;
import com.developmentontheedge.sql.format.ContextApplier;
import com.developmentontheedge.sql.format.Formatter;
import com.developmentontheedge.sql.format.LimitsApplier;
import com.developmentontheedge.sql.format.QueryContext;
import com.developmentontheedge.sql.format.Simplifier;
import com.developmentontheedge.sql.model.AstBeSqlSubQuery;
import com.developmentontheedge.sql.model.AstStart;
import com.developmentontheedge.sql.model.DefaultParserContext;
import com.developmentontheedge.sql.model.ParserContext;
import com.developmentontheedge.sql.model.SqlQuery;

import one.util.streamex.StreamEx;

import javax.inject.Provider;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.developmentontheedge.be5.api.FrontendConstants.CATEGORY_ID_PARAM;
import static com.developmentontheedge.be5.query.impl.utils.QueryUtils.*;


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
            return StreamEx.of(UserInfoHolder.getCurrentRoles());
        }

        @Override
        public String resolveQuery(String entityName, String queryName)
        {
            return meta.getQueryCode( entityName == null ? query.getEntity().getName() : entityName, queryName );
        }

        @Override
        public String getUserName()
        {
            return UserInfoHolder.getUserName();
        }

        @Override
        public Object getSessionVariable(String name)
        {
            return UserInfoHolder.getSession().get(name);
        }

        @Override
        public String getParameter(String name)
        {
            if( parameters.get( name ) == null )
                return null;
            if( parameters.get( name ).size() != 1 )
                throw new IllegalStateException( name+ " contains more than one value" );
            else
                return parameters.get( name ).get( 0 );
        }

        @Override
        public List<String> getListParameter(String name)
        {
            return parameters.get(name);
        }

        @Override
        public Map<String, String> asMap()
        {
            return StreamEx.ofKeys( parameters ).toMap( this::getParameter );
        }

        @Override
        public String getDictionaryValue(String tagName, String name, Map<String, String> conditions)
        {
            EntityModel entityModel = database.get().getEntity(tagName);
            RecordModel row = entityModel.getBy(conditions);

            String value = row.getValue(name).toString();

            if(!meta.isNumericColumn(entityModel.getEntity(), name))
            {
                value = "'" + value + "'";
            }

            return value;
        }
    }

    private final DatabaseService databaseService;
    private final ConnectionService connectionService;
    private final Provider<DatabaseModel> database;
    private final Meta meta;
    private final SqlService db;

    private final Map<String, List<String>> parameters;

    private final Context context;
    private ExecutorQueryContext executorQueryContext;
    private ContextApplier contextApplier;
    private final ParserContext parserContext;
    private Set<String> subQueryKeys;
    private ExecuteType executeType;


    public Be5QueryExecutor(Query query, ConnectionService connectionService, Map<String, List<String>> parameters, DatabaseService databaseService,
                            Provider<DatabaseModel> database, Meta meta, SqlService db)
    {
        super(query);
        this.connectionService = connectionService;

        this.parameters = parameters;
        this.databaseService = databaseService;
        this.database = database;
        this.meta = meta;
        this.db = db;

        this.executorQueryContext = new ExecutorQueryContext();
        this.contextApplier = new ContextApplier( executorQueryContext );
        this.context = new Context( databaseService.getDbms() );
        this.parserContext = new DefaultParserContext();
        this.subQueryKeys = Collections.emptySet();
        this.executeType = ExecuteType.DEFAULT;

        selectable = !query.getOperationNames().isEmpty() && query.getType() == QueryType.D1;
    }

    @Override
    public <T> List<T> execute(ResultSetParser<T> parser)
    {
        if ( query.getType().equals(QueryType.D1) || query.getType().equals(QueryType.D1_UNKNOWN ))
        {
            try
            {
                return db.selectList(getFinalSql(), parser);
            }
            catch (RuntimeException e)
            {
                throw Be5Exception.internalInQuery(e, query);
            }
        }

        throw new UnsupportedOperationException("Query type " + query.getType() + " is not supported yet");
    }

    @Override
    public List<String> getColumnNames() throws Be5Exception
    {
        if ( query.getType().equals(QueryType.D1) || query.getType().equals(QueryType.D1_UNKNOWN) )
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
        if(queryText.isEmpty())
            return "";
        AstStart ast;
        try
        {
            ast = SqlQuery.parse(queryText);
        }
        catch (RuntimeException e)
        {
            log.log(Level.SEVERE, "SqlQuery.parse error: " , e);
            throw Be5Exception.internalInQuery(e, query);

            //ast = SqlQuery.parse("select 'error'");
        }
        dql.log("Compiled", ast);

        resolveTypeOfRefColumn(ast, meta);

        // FILTERS
        applyFilters(ast, query.getEntity().getName(), resolveTypes(parameters, meta));

        // CATEGORY
        applyCategory( dql, ast );

        // CONTEXT
        contextApplier.applyContext( ast );
        subQueryKeys = contextApplier.subQueryKeys().toSet();
        dql.log("With context", ast);

        // ID COLUMN
        addIDColumnIfNeeded(ast, query, dql);

        // SIMPLIFY
        Simplifier.simplify(ast);
        dql.log("Simplified", ast);

        if(executeType == ExecuteType.COUNT)
        {
            countFromQuery(ast.getQuery());
            dql.log("Count(1) from query", ast);
        }

        if(executeType == ExecuteType.DEFAULT)
        {
            // SORT ORDER
            applySort(ast, getSchema(new Formatter().format(ast, context, parserContext)), dql,
                    getOrderColumn(), getOrderDir());

            // LIMITS
            new LimitsApplier( offset, limit ).transform( ast );
            dql.log("With limits", ast);
        }

        return new Formatter().format( ast, context, parserContext );
    }

    private DynamicProperty[] getSchema(String sql)
    {
        try{
            Connection conn = connectionService.getConnection(true);

            try(PreparedStatement ps = conn.prepareStatement(sql)) {
                return DpsRecordAdapter.createSchema(ps.getMetaData());
            }
            finally {
                connectionService.releaseConnection(conn);
            }
        }
        catch (Throwable e)
        {
            log.log(Level.FINE, "fail getSchema, return empty", e);
            return new DynamicProperty[]{};
        }
    }

    private void applyCategory(DebugQueryLogger dql, AstStart ast)
    {
        String categoryString = executorQueryContext.getParameter(CATEGORY_ID_PARAM);
        if(categoryString != null)
        {
            long categoryId;
            try
            {
                categoryId = Long.parseLong(categoryString);
            }
            catch( NumberFormatException e )
            {
                throw Be5Exception.internalInQuery(
                        new IllegalArgumentException( "Invalid category: " + categoryString, e ), query );
            }

            new CategoryFilter(query.getEntity().getName(), query.getEntity().getPrimaryKey(), categoryId).apply( ast );
            dql.log("With category", ast);
        }
    }

    private List<String> getColumnNames(String sql)
    {
        return db.select(sql, rs -> {
            List<String> result = new ArrayList<>();
            ResultSetMetaData meta = rs.getMetaData();

            for (int column = 1, count = meta.getColumnCount(); column <= count; column++) {
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
//                    .newInstance( UserInfoHolder.getUserInfo(), new MapParamHelper(parameters), connector, offset, limit );
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
//            AstBeSqlSubQuery subQuery = contextApplier.applyVars((String) value, s -> "");
//            meta.put("sql", StreamEx.of("beautifier", "default").mapToEntry(subQuery::getParameter).nonNullValues().toSortedMap());
//        }
//    }

    @Override
    public List<DynamicPropertySet> execute()
    {
        executeType = ExecuteType.DEFAULT;
        return execute(DpsRecordAdapter::createDps);
    }

    @Override
    public List<DynamicPropertySet> executeAggregate()
    {
        executeType = ExecuteType.AGGREGATE;
        return execute(DpsRecordAdapter::createDps);
    }

    @Override
    public long count()
    {
        executeType = ExecuteType.COUNT;
        return (Long)execute(DpsRecordAdapter::createDps).get(0).asMap().get("count");
    }

    @Override
    public DynamicPropertySet getRow()
    {
        return execute(DpsRecordAdapter::createDps).get(0);
    }

    @Override
    public List<DynamicPropertySet> executeSubQuery(String subqueryName, CellFormatter.VarResolver varResolver)
    {
        AstBeSqlSubQuery subQuery = contextApplier.applyVars(subqueryName, varResolver::resolve);

        if(subQuery.getQuery() == null)
        {
            return Collections.emptyList();
        }

        String finalSql = new Formatter().format(subQuery.getQuery(), context, parserContext);

        List<DynamicPropertySet> dynamicPropertySets;

        try
        {
            dynamicPropertySets = db.selectList(finalSql, DpsRecordAdapter::createDps);
        }
        catch (Throwable e)
        {
            //TODO only for Document presentation, for operations must be error throw
            Be5Exception be5Exception = Be5Exception.internalInQuery(e, query);
            log.log(Level.SEVERE, be5Exception.toString() + " Final SQL: " + finalSql, be5Exception);

            DynamicPropertySetSupport dynamicProperties = new DynamicPropertySetSupport();
            dynamicProperties.add(new DynamicProperty("___ID", String.class, "-1"));
            dynamicProperties.add(new DynamicProperty("error", String.class, UserInfoHolder.isSystemDeveloper() ? Be5Exception.getMessage(e) : "error"));
            dynamicPropertySets =  Collections.singletonList(dynamicProperties);
        }

        return dynamicPropertySets;
    }

}
