package com.developmentontheedge.be5.query.impl.model;

import com.developmentontheedge.be5.api.helpers.FilterHelper;
import com.developmentontheedge.be5.api.sql.DpsRecordAdapter;
import com.developmentontheedge.be5.api.services.Meta;
import com.developmentontheedge.be5.databasemodel.EntityModel;
import com.developmentontheedge.be5.databasemodel.RecordModel;
import com.developmentontheedge.be5.databasemodel.impl.DatabaseModel;
import com.developmentontheedge.be5.env.Injector;
import com.developmentontheedge.be5.api.exceptions.Be5Exception;
import com.developmentontheedge.be5.api.helpers.UserInfoHolder;
import com.developmentontheedge.be5.api.services.DatabaseService;
import com.developmentontheedge.be5.api.services.SqlService;
import com.developmentontheedge.be5.api.sql.ResultSetParser;
import com.developmentontheedge.be5.metadata.DatabaseConstants;
import com.developmentontheedge.be5.metadata.QueryType;
import com.developmentontheedge.be5.metadata.model.Entity;
import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.beans.DynamicProperty;
import com.developmentontheedge.beans.DynamicPropertySet;
import com.developmentontheedge.beans.DynamicPropertySetSupport;
import com.developmentontheedge.sql.format.Ast;
import com.developmentontheedge.sql.format.ColumnAdder;
import com.developmentontheedge.sql.format.Context;
import com.developmentontheedge.sql.format.ContextApplier;
import com.developmentontheedge.sql.format.Formatter;
import com.developmentontheedge.sql.format.LimitsApplier;
import com.developmentontheedge.sql.format.QueryContext;
import com.developmentontheedge.sql.format.Simplifier;
import com.developmentontheedge.sql.model.AstBeParameterTag;
import com.developmentontheedge.sql.model.AstBeSqlSubQuery;
import com.developmentontheedge.sql.model.AstDerivedColumn;
import com.developmentontheedge.sql.model.AstIdentifierConstant;
import com.developmentontheedge.sql.model.AstLimit;
import com.developmentontheedge.sql.model.AstNumericConstant;
import com.developmentontheedge.sql.model.AstOrderBy;
import com.developmentontheedge.sql.model.AstOrderingElement;
import com.developmentontheedge.sql.model.AstParenthesis;
import com.developmentontheedge.sql.model.AstQuery;
import com.developmentontheedge.sql.model.AstSelect;
import com.developmentontheedge.sql.model.AstStart;
import com.developmentontheedge.sql.model.AstTableRef;
import com.developmentontheedge.sql.model.DefaultParserContext;
import com.developmentontheedge.sql.model.ParserContext;
import com.developmentontheedge.sql.model.SqlQuery;
import com.developmentontheedge.sql.model.Token;

import one.util.streamex.MoreCollectors;
import one.util.streamex.StreamEx;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;


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
            return meta.getQueryCode( entityName == null ? query.getEntity().getName() : entityName,
                    queryName, UserInfoHolder.getCurrentRoles() );
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
            return parametersMap.get(name);
        }

        @Override
        public List<String> getListParameter(String name)
        {
            String value = parametersMap.get(name);
            return value == null ? null : Collections.singletonList(value);
        }

        @Override
        public Map<String, String> asMap()
        {
            return parametersMap;
        }

        @Override
        public String getDictionaryValue(String tagName, String name, Map<String, String> conditions)
        {
            EntityModel entityModel = database.getEntity(tagName);
            RecordModel row = entityModel.get(conditions);

            String value = row.getValue(name).toString();

            if(!meta.isNumericColumn(entityModel.getEntity(), name))
            {
                value = "'" + value + "'";
            }

            return value;
        }
    }

    private final Map<String, String> parametersMap;
    private final DatabaseService databaseService;
    private final DatabaseModel database;
    private final Meta meta;
    private final SqlService db;
    private final Context context;
    private final FilterHelper filterHelper;

    private ContextApplier contextApplier;
    private final ParserContext parserContext;
    private Set<String> subQueryKeys;
    private ExecuteType executeType;


    public Be5QueryExecutor(Query query, Map<String, String> parameters, Injector injector)
    {
        super(query);
        this.databaseService = injector.getDatabaseService();
        this.database        = injector.get(DatabaseModel.class);
        this.meta            = injector.getMeta();
        this.db              = injector.getSqlService();
        this.filterHelper    = injector.get(FilterHelper.class);

        this.parametersMap = new HashMap<>( Objects.requireNonNull( parameters ) );
        this.contextApplier = new ContextApplier( new ExecutorQueryContext() );
        this.context = new Context( databaseService.getRdbms().getDbms() );
        this.parserContext = new DefaultParserContext();
        this.subQueryKeys = Collections.emptySet();
        this.executeType = ExecuteType.DEFAULT;

        if(selectable == null)
        {
            selectable = !query.getOperationNames().isEmpty() && query.getType() == QueryType.D1;
        }
    }

    @Override
    public <T> List<T> execute(ResultSetParser<T> parser) throws Be5Exception
    {
        if ( query.getType().equals(QueryType.D1) || query.getType().equals(QueryType.D1_UNKNOWN ) )
            return getResults(getFinalSql(), parser);
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

        String queryText = meta.getQueryCode(query, UserInfoHolder.getCurrentRoles());

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

        resolveTypeOfRefColumn(ast);

        // CONTEXT

        // FILTERS
        filterHelper.applyFilters(ast, query.getEntity().getName(), new HashMap<>(parametersMap));

        // CATEGORY
        //applyCategory( dql, ast );

        contextApplier.applyContext( ast );
        subQueryKeys = contextApplier.subQueryKeys().toSet();
        dql.log("With context", ast);

        // ID COLUMN
        if( query.getType() == QueryType.D1 && query.getEntity().findTableDefinition() != null && !hasColumnWithLabel(ast, DatabaseConstants.ID_COLUMN_LABEL) )
        {
            new ColumnAdder().addColumn( ast, query.getEntity().getName(), query.getEntity().getPrimaryKey(),
                    DatabaseConstants.ID_COLUMN_LABEL );
            dql.log("With ID column", ast);
        }
        else
        {
            dql.log("Without ID column", ast);
        }

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
            applySort(dql, ast);

            // LIMITS
            new LimitsApplier( offset, limit ).transform( ast );
            dql.log("With limits", ast);
        }

        return new Formatter().format( ast, context, parserContext );
    }

    private void resolveTypeOfRefColumn(AstStart ast)
    {
        ast.tree().select(AstBeParameterTag.class).forEach(tag -> {
            if(tag.getRefColumn() != null)
            {
                String[] split = tag.getRefColumn().split("\\.");
                String table, column;
                if(split.length == 2)
                {
                    table = split[0];
                    column = split[1];
                }
                else if(split.length == 3)
                {
                    table = split[0] + "." + split[1];
                    column = split[2];
                }
                else
                {
                    return;
                }
                Entity entity = meta.getEntity(table);
                if(entity != null)
                {
                    tag.setType(meta.getColumnType(entity, column).getName());
                }
            }
        });
    }

    private void countFromQuery(AstQuery query)
    {
        AstSelect select = Ast.selectCount().from(AstTableRef.as(
                new AstParenthesis( query.clone() ),
                new AstIdentifierConstant( "data", true )
        ));
        query.replaceWith( new AstQuery( select ) );
    }

    private boolean hasColumnWithLabel(AstStart ast, String idColumnLabel)
    {
        AstQuery query = ast.getQuery();
        Optional<AstSelect> selectOpt = query.children().select(AstSelect.class).collect(MoreCollectors.onlyOne());
        if(!selectOpt.isPresent())
            return false;
        AstSelect select = selectOpt.get();
        return select.getSelectList().children()
            .select(AstDerivedColumn.class)
            .map(AstDerivedColumn::getAlias)
            .nonNull()
            .map(alias -> alias.replaceFirst("^\"(.+)\"$", "$1"))
            .has(idColumnLabel);
    }

    private void applySort(DebugQueryLogger dql, AstStart ast)
    {
        if(getOrderColumn() >= 0) {
            try
            {
                DynamicProperty[] schema = getSchema(new Formatter().format(ast, context, parserContext));
                int sortCol = getQuerySortingColumn(schema);
                if(sortCol > 0) {
                    AstSelect sel = (AstSelect)ast.getQuery().jjtGetChild(
                            ast.getQuery().jjtGetNumChildren()-1);

                    AstOrderBy orderBy = sel.getOrderBy();
                    if(orderBy == null) {
                        orderBy = new AstOrderBy();
                        sel.addChild(orderBy);
                        AstLimit astLimit = sel.children().select(AstLimit.class).findFirst().orElse(null);
                        if(astLimit != null){
                            sel.removeChild(astLimit);
                            sel.addChild(astLimit);
                        }
                    }
                    AstOrderingElement oe = new AstOrderingElement(AstNumericConstant.of(sortCol));
                    if(orderDesc) {
                        oe.setDirectionToken(new Token(0, "DESC"));
                    }
                    orderBy.addChild(oe);
                    orderBy.moveToFront(oe);
                }
                dql.log("With sort", ast);
            }
            catch (SQLException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    private int getQuerySortingColumn(DynamicProperty[] schema)
    {
        int sortCol = -1;
        int restCols = getOrderColumn();
        for(int i=0; i<schema.length; i++) {
            if(schema[i].isHidden())continue;

            if(restCols--==0) {
                sortCol = i+1;
                break;
            }
        }
        return sortCol;
    }

    private DynamicProperty[] getSchema(String sql) throws SQLException
    {
        Connection conn = databaseService.getConnection(true);

        try(PreparedStatement ps = conn.prepareStatement(sql)) {
            return DpsRecordAdapter.createSchema(ps.getMetaData());
        }
        finally {
            databaseService.releaseConnection(conn);
        }
    }
//
//    private void applyCategory(DebugQueryLogger dql, AstStart ast)
//    {
//        String categoryString = parametersMap.get( "category" );
//        if(categoryString != null)
//        {
//            long categoryId;
//            try
//            {
//                categoryId = Long.parseLong(categoryString);
//            }
//            catch( NumberFormatException e )
//            {
//                IllegalArgumentException e2 = new IllegalArgumentException("Invalid category: " + categoryString, e);
//                log.log(Level.SEVERE, e2.toString() + query.getEntity().getName(), e2);
//                throw Be5Exception.internalInQuery( new IllegalArgumentException( "Invalid category: " + categoryString ),
//                        query );
//            }
//            new CategoryFilter(query.getEntity().getName(), query.getEntity().getPrimaryKey(), categoryId).apply( ast );
//            dql.log("With category", ast);
//        }
//    }

    private <T> List<T> getResults(String sql, ResultSetParser<T> parser)
    {
        return db.selectList(sql, parser);
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
//                    .newInstance( UserInfoHolder.getUserInfo(), new MapParamHelper(parametersMap), connector, offset, limit );
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

    private List<DynamicPropertySet> listDps(String finalSql)
    {
        try
        {
            return db.selectList(finalSql, DpsRecordAdapter::createDps);
        }
        catch (RuntimeException e)
        {
            throw Be5Exception.internalInQuery(e, query);
        }
    }

//    private void processMeta(Object value, Map<String, Map<String, String>> meta)
//    {
//        if (subQueryKeys.contains(value) && !meta.containsKey("sql"))
//        {
//            AstBeSqlSubQuery subQuery = contextApplier.applyVars((String) value, s -> "");
//            meta.put("sql", StreamEx.of("beautifier", "default").mapToEntry(subQuery::getParameter).nonNullValues().toSortedMap());
//        }
//    }

    static class DebugQueryLogger
    {
        private static final Logger log = Logger.getLogger(DebugQueryLogger.class.getName());
        private String lastQuery;

        public void log(String name, AstStart ast)
        {
            log(name, ast.format());
        }

        public void log(String name, String query)
        {
            if(!query.equals(lastQuery)) {
                StringBuilder sb = new StringBuilder();
                sb.append(name).append(": ");
                if(lastQuery == null) {
                    sb.append(query);
                } else {
                    String prefix = StreamEx.of(query, lastQuery).collect(MoreCollectors.commonPrefix());
                    String suffix = StreamEx.of(query, lastQuery).collect(MoreCollectors.commonSuffix());
                    int startPos = prefix.length();
                    int endPos = query.length() - suffix.length();
                    startPos = startPos > 10 ? query.lastIndexOf('\n', startPos-10) : 0;
                    endPos = suffix.length() > 10 ? query.indexOf('\n', endPos+10) : query.length();
                    if(startPos < 0)
                        startPos = 0;
                    if(endPos < 0)
                        endPos = query.length();
                    String substring = query.substring(startPos, endPos);
                    if(startPos > 0)
                        substring = "..."+substring.substring(1);
                    if(endPos < query.length())
                        substring += "...";
                    sb.append(substring);
                }
                log.finer(sb.toString());
                lastQuery = query;
            }
        }
    }

    @Override
    public List<DynamicPropertySet> execute()
    {
        executeType = ExecuteType.DEFAULT;
        return listDps(getFinalSql());
    }

    @Override
    public List<DynamicPropertySet> executeAggregate()
    {
        executeType = ExecuteType.AGGREGATE;
        return listDps(getFinalSql());
    }

    @Override
    public long count()
    {
        executeType = ExecuteType.COUNT;
        return (Long)listDps(getFinalSql()).get(0).asMap().get("count");
    }

    @Override
    public DynamicPropertySet getRow()
    {
        return listDps(getFinalSql()).get(0);
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
            dynamicPropertySets = listDps(finalSql);
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


//        TableModel table = TableModel
//                .from(meta.createQueryFromSql(subQuery.getQuery().format()), parametersMap, session, false, injector)
//                .setContextApplier(contextApplier)
//                .build();
//
//        String innerJoinedTable = print(table);
//        DynamicPropertySetSupport dynamicProperties = new DynamicPropertySetSupport();
//        dynamicProperties.add(new DynamicProperty("innerJoinedTable", String.class, innerJoinedTable));


        //return Collections.singletonList(dynamicProperties);
        return dynamicPropertySets;
    }

//    private String print(TableModel tableModel)
//    {
//        return StreamEx.of(tableModel.getRows())
//                .map(rowModel -> StreamEx.of(rowModel.getCells()).map(cellModel -> cellModel.content != null ? cellModel.content.toString() : "").joining(" "))
//                .joining("<br/> ");
//    }
//
//    @Override
//    public QueryExecutor setContextApplier(ContextApplier contextApplier)
//    {
//        this.contextApplier = contextApplier;
//        return this;
//    }
}
