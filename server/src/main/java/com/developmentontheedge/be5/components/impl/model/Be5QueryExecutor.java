package com.developmentontheedge.be5.components.impl.model;

import com.developmentontheedge.be5.api.Request;
import com.developmentontheedge.be5.api.ServiceProvider;
import com.developmentontheedge.be5.api.exceptions.Be5Exception;
import com.developmentontheedge.be5.api.helpers.UserAwareMeta;
import com.developmentontheedge.be5.api.helpers.UserInfoHolder;
import com.developmentontheedge.be5.api.services.DatabaseService;
import com.developmentontheedge.be5.api.services.DpsStreamer;
import com.developmentontheedge.be5.api.services.SqlService;
import com.developmentontheedge.be5.api.sql.ResultSetParser;
import com.developmentontheedge.be5.components.impl.model.TableModel.RawCellModel;
import com.developmentontheedge.be5.metadata.DatabaseConstants;
import com.developmentontheedge.be5.metadata.QueryType;
import com.developmentontheedge.be5.metadata.exception.ProjectElementException;
import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.util.HashUrl;
import com.developmentontheedge.be5.util.Unzipper;
import com.developmentontheedge.beans.DynamicProperty;
import com.developmentontheedge.beans.DynamicPropertySet;
import com.developmentontheedge.beans.DynamicPropertySetAsMap;
import com.developmentontheedge.sql.format.CategoryFilter;
import com.developmentontheedge.sql.format.ColumnAdder;
import com.developmentontheedge.sql.format.ColumnRef;
import com.developmentontheedge.sql.format.Context;
import com.developmentontheedge.sql.format.ContextApplier;
import com.developmentontheedge.sql.format.FilterApplier;
import com.developmentontheedge.sql.format.Formatter;
import com.developmentontheedge.sql.format.LimitsApplier;
import com.developmentontheedge.sql.format.QueryContext;
import com.developmentontheedge.sql.format.Simplifier;
import com.developmentontheedge.sql.model.AstBeParameterTag;
import com.developmentontheedge.sql.model.AstBeSqlSubQuery;
import com.developmentontheedge.sql.model.AstCount;
import com.developmentontheedge.sql.model.AstDerivedColumn;
import com.developmentontheedge.sql.model.AstFrom;
import com.developmentontheedge.sql.model.AstIdentifierConstant;
import com.developmentontheedge.sql.model.AstLimit;
import com.developmentontheedge.sql.model.AstNumericConstant;
import com.developmentontheedge.sql.model.AstOrderBy;
import com.developmentontheedge.sql.model.AstOrderingElement;
import com.developmentontheedge.sql.model.AstParenthesis;
import com.developmentontheedge.sql.model.AstQuery;
import com.developmentontheedge.sql.model.AstSelect;
import com.developmentontheedge.sql.model.AstSelectList;
import com.developmentontheedge.sql.model.AstStart;
import com.developmentontheedge.sql.model.AstTableRef;
import com.developmentontheedge.sql.model.DefaultParserContext;
import com.developmentontheedge.sql.model.ParserContext;
import com.developmentontheedge.sql.model.SqlQuery;
import com.developmentontheedge.sql.model.Token;

import com.google.common.collect.ImmutableList;
import one.util.streamex.EntryStream;
import one.util.streamex.MoreCollectors;
import one.util.streamex.StreamEx;

import javax.servlet.http.HttpSession;
import java.io.PrintStream;
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
import java.util.regex.Pattern;


/**
 * A modern query executor that uses our new parser.
 */
public class Be5QueryExecutor extends AbstractQueryExecutor
{
    private static final Logger log = Logger.getLogger(Be5QueryExecutor.class.getName());

    private enum ExtraQuery {
        DEFAULT, COUNT, AGGREGATE
    }

    private static final Pattern SUBQUERY_PATTERN = Pattern.compile("<sql>SubQuery#[0-9]+</sql>");

    private final class ExecutorQueryContext implements QueryContext
    {
        @Override
        public StreamEx<String> roles()
        {
            return StreamEx.of(UserInfoHolder.getCurrentRoles());
        }

        @Override
        public String resolveQuery(String entityName, String queryName)
        {
            Query subQuery = userAwareMeta.getQuery( entityName == null ? query.getEntity().getName() : entityName, queryName );
            try
            {
                return subQuery.getQueryCompiled().validate();
            }
            catch( ProjectElementException e )
            {
                throw Be5Exception.internal( e );
            }
        }

        @Override
        public String getUserName()
        {
            return UserInfoHolder.getUserName();
        }

        @Override
        public String getSessionVariable(String name)
        {
            Object attr = session.getAttribute(name);
            return attr != null ? attr.toString() : "";
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
    }

    private class CellFormatter
    {
        private final RawCellModel cell;
        private final VarResolver rootVarResolver;


        public CellFormatter(RawCellModel cell, VarResolver rootVarResolver)
        {
            this.cell = cell;
            this.rootVarResolver = rootVarResolver;
        }

        public Object format()
        {
            return formatCell(cell.content, rootVarResolver);
        }

        private Object formatCell(String cellContent, VarResolver varResolver)
        {
            ImmutableList<Object> formattedParts = getFormattedPartsWithoutLink(cellContent, varResolver);

            String formattedContent = StreamEx.of(formattedParts).map(this::print).joining();

            formattedContent = userAwareMeta.getLocalizedCell(formattedContent, query.getEntity().getName(), query.getName());

            if(formattedContent != null && extraQuery == ExtraQuery.DEFAULT) {

                Map<String, String> blankNullsProperties = cell.options.get(DatabaseConstants.COL_ATTR_BLANKNULLS);
                if(blankNullsProperties != null)
                {
                    if( formattedContent.equals( "null" ) )
                    {
                        formattedContent = blankNullsProperties.getOrDefault("value", "");
                    }
                }


                Map<String, String> nullIfProperties = cell.options.get(DatabaseConstants.COL_ATTR_NULLIF);
                if(nullIfProperties != null)
                {
                    if( formattedContent.equals( nullIfProperties.get("value") ) )
                    {
                        formattedContent = nullIfProperties.getOrDefault("result", "");
                    }
                }

                Map<String, String> linkProperties = cell.options.get(DatabaseConstants.COL_ATTR_LINK);
                if(linkProperties != null)
                {
                    HashUrl url = new HashUrl("table").positional(linkProperties.get("table"))
                            .positional(linkProperties.getOrDefault("queryName", DatabaseConstants.ALL_RECORDS_VIEW));
                    String cols = linkProperties.get("columns");
                    String vals = linkProperties.get("using");
                    if(cols != null && vals != null)
                    {
                        url = url.named(EntryStream.zip(cols.split(","), vals.split(",")).mapValues(varResolver::resolve).toMap());
                    }
                    return "<a href=\"#!"+url+"\">"+formattedContent+"</a>";
                }

            }

            return formattedContent;
        }

        ImmutableList<Object> getFormattedPartsWithoutLink(String cellContent, VarResolver varResolver){
            boolean hasLink = cell != null && cell.options.containsKey("link");
            Map<String, String> link = null;
            if(hasLink) {
                link = cell.options.get("link");
                cell.options.remove("link");
            }

            ImmutableList.Builder<Object> builder = ImmutableList.builder();

            Unzipper.on(SUBQUERY_PATTERN).trim().unzip(cellContent, builder::add, subquery -> {
                builder.add(applySubqueryIfPossible(subquery, varResolver));
            });

            ImmutableList<Object> formattedParts = builder.build();

            if(hasLink) {
                cell.options.put("link", link);
            }

            return formattedParts;
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
                return StreamEx.of(table).map(list -> StreamEx.of(list).map(this::print).joining(", ")).joining("; ");
            }
            else
            {
                throw new AssertionError(formattedPart.getClass().getName());
            }
        }

        private Object applySubqueryIfPossible(String subqueryName, VarResolver varResolver)
        {
            AstBeSqlSubQuery subQuery = contextApplier.applyVars(subqueryName, varResolver::resolve);

            if (subQuery != null)
            {
                return getSubqueryResult(subQuery, varResolver);
            }

            return subqueryName;
        }

        private List<List<Object>> getSubqueryResult(AstBeSqlSubQuery subQuery, VarResolver varResolver)
        {
            String finalSQL = new Formatter().format(subQuery.getQuery(), context, parserContext);

            try (StreamEx<DynamicPropertySet> stream = stream(finalSQL))
            {
                return toTable(stream, varResolver);
            }
        }

        /**
         * Returns a two-dimensional list of processed content. Each element is either a string or a table.
         */
        private List<List<Object>> toTable(StreamEx<DynamicPropertySet> stream, VarResolver varResolver)
        {
            return stream.map(dps -> toRow(dps, varResolver)).toList();
        }

        /**
         * Transforms a set of properties to a list. Each element of the list is a string or a table.
         */
        private List<Object> toRow(DynamicPropertySet dps, VarResolver varResolver)
        {
            DynamicPropertySet previousCells = new DynamicPropertySetAsMap();

            return StreamEx.of(dps.spliterator()).map(property -> {
                String name = property.getName();
                Object value = property.getValue();
                Object processedCell = formatCell(String.valueOf(value), new CompositeVarResolver(new RootVarResolver(previousCells), varResolver));
                previousCells.add(new DynamicProperty(name, String.class, processedCell));
                return processedCell;
            }).toList();
        }

    }

    private final Map<String, String> parametersMap;
    private final DatabaseService databaseService;
    private final SqlService db;
    private final HttpSession session;
    private final ContextApplier contextApplier;
    private final UserAwareMeta userAwareMeta;
    private final Context context;
    private final ParserContext parserContext;
    private final ServiceProvider serviceProvider;
    private final DpsStreamer dpsStreamer;
    private Set<String> subQueryKeys;
    private final ExecutorQueryContext queryContext;
    private ExtraQuery extraQuery;

    /**
     * Note that the request is not used to get parameters, so parameters can be formed manually in any way.
     */
    public Be5QueryExecutor(Query query, Map<String, String> parameters, Request req, ServiceProvider serviceProvider)
    {
        super(query);
        this.parametersMap = new HashMap<>( Objects.requireNonNull( parameters ) );
        this.databaseService = serviceProvider.getDatabaseService();
        this.db = serviceProvider.getSqlService();
        this.userAwareMeta = UserAwareMeta.get(req, serviceProvider);
        this.session = req.getRawSession();
        this.queryContext = new ExecutorQueryContext();
        this.contextApplier = new ContextApplier( queryContext );
        this.context = new Context( databaseService.getRdbms().getDbms() );
        this.parserContext = new DefaultParserContext();
        this.subQueryKeys = Collections.emptySet();
        this.serviceProvider = serviceProvider;
        this.dpsStreamer = serviceProvider.get(DpsStreamer.class);
        this.extraQuery = ExtraQuery.DEFAULT;
    }

    private StreamEx<DynamicPropertySet> executeQuery()
    {
        switch (query.getType())
        {
//        case Query.QUERY_TYPE_CUSTOM:
//            return streamCustomQuery();
        case D1:
            return stream1DQuery();
        case D1_UNKNOWN:
            return stream1DUnknownQuery();
        default:
            // TODO: support other query types
            throw new UnsupportedOperationException("Query type " + query.getType() + " is not supported yet");
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

    private StreamEx<DynamicPropertySet> stream1DQuery()
    {
        String sql = getFinalSql();
        return sql == null ? StreamEx.empty() : stream(sql);
    }

    private StreamEx<DynamicPropertySet> stream1DUnknownQuery()
    {
        String sql = getFinalSql();
        return sql == null ? StreamEx.empty() : stream(sql);
    }

    private String getFinalSql()
    {
        DebugQueryLogger dql = new DebugQueryLogger(System.out);
        dql.log("Orig", query.getQuery());
        String queryText;
        try
        {
            synchronized(query.getProject())
            {
                queryText = query.getQueryCompiled().validate().trim();
            }
        }
        catch( ProjectElementException e )
        {
            throw Be5Exception.internalInQuery( e, query );
        }
        dql.log("After FreeMarker", queryText);
        if(queryText.isEmpty())
            return null;
        AstStart ast = SqlQuery.parse( queryText );
        dql.log("Compiled", ast);

        // CONTEXT
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

        // FILTERS
        applyFilters(ast);

        // CATEGORY
        applyCategory( dql, ast );

        // SIMPLIFY
        Simplifier.simplify(ast);
        dql.log("Simplified", ast);

        if(extraQuery == ExtraQuery.COUNT){
            countFromQuery(ast.getQuery());
            dql.log("Count(1) from query", ast);
        }
        if(extraQuery == ExtraQuery.DEFAULT){
            // SORT ORDER
            applySort(dql, ast);

            // LIMITS
            new LimitsApplier( offset, limit ).transform( ast );
            dql.log("With limits", ast);
        }

        String finalSQL = new Formatter().format( ast, context, parserContext );
        log.info("Final SQL: " + finalSQL);

        return finalSQL;
    }

    protected static void countFromQuery(AstQuery query) {
        AstTableRef tableRef = new AstTableRef(
                true,
                new AstParenthesis( query.clone() ),
                new AstIdentifierConstant( "data", true )
        );
        AstSelect select = new AstSelect( new AstSelectList(new AstCount()), new AstFrom( tableRef ) );
        query.replaceWith( new AstQuery( select ) );
    }

    private void applyFilters(AstStart ast) {
        DebugQueryLogger dql = new DebugQueryLogger(System.out);
        Set<String> usedParams = ast.tree().select(AstBeParameterTag.class).map(AstBeParameterTag::getName).toSet();

        Map<ColumnRef, String> filters = EntryStream.of(parametersMap)
                .removeKeys(usedParams::contains)
                .removeKeys("category"::equals)
                .mapKeys(k -> ColumnRef.resolve(ast, k.contains(".") ? k : query.getEntity().getName() + "." + k))
                .nonNullKeys().toMap();
        if(!filters.isEmpty())
        {
            new FilterApplier().addFilter(ast, filters);
            dql.log("With filters", ast);
        }
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
            .map(dc -> dc.getAlias())
            .nonNull()
            .map(alias -> alias.replaceFirst("^\"(.+)\"$", "$1"))
            .has(idColumnLabel);
    }

    private void applySort(DebugQueryLogger dql, AstStart ast)
    {
        if(sortColumn >= 0) {
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
                    if(sortDesc) {
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
        int restCols = sortColumn;
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
            return dpsStreamer.createSchema(ps.getMetaData());
        }
        finally {
            databaseService.releaseConnection(conn);
        }
    }

    private void applyCategory(DebugQueryLogger dql, AstStart ast)
    {
        String categoryString = parametersMap.get( "category" );
        if(categoryString != null)
        {
            long categoryId;
            try
            {
                categoryId = Long.parseLong(categoryString);
            }
            catch( NumberFormatException e )
            {
                IllegalArgumentException e2 = new IllegalArgumentException("Invalid category: " + categoryString, e);
                log.log(Level.SEVERE, e2.toString() + query.getEntity().getName(), e2);
                throw Be5Exception.internalInQuery( new IllegalArgumentException( "Invalid category: " + categoryString ),
                        query );
            }
            new CategoryFilter(query.getEntity().getName(), query.getEntity().getPrimaryKey(), categoryId).apply( ast );
            dql.log("With category", ast);
        }
    }

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
//                ((Be5Query) iterator).initialize(serviceProvider);
//            }
//
//            @SuppressWarnings("unchecked")
//            StreamEx<DynamicPropertySet> stream = StreamEx.of( iterator );
//            return stream;
//        }
//        catch( InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
//                | NoSuchMethodException | SecurityException | ProjectElementException e )
//        {
//            throw Be5Exception.internalInQuery( e, query );
//        }
//    }

    private StreamEx<DynamicPropertySet> stream(String finalSql)
    {
        try
        {
            return dpsStreamer.stream(finalSql, this::processMeta);
        }
        catch (Exception e)
        {
            log.log(Level.SEVERE, e.toString() + " Final SQL: " + finalSql, e);
            throw Be5Exception.internalInQuery(e, query);
        }
    }

    private void processMeta(Object value, Map<String, Map<String, String>> meta)
    {
        if (subQueryKeys.contains(value) && !meta.containsKey("sql"))
        {
            AstBeSqlSubQuery subQuery = contextApplier.applyVars((String) value, s -> "");
            meta.put("sql", StreamEx.of("beautifier", "default").mapToEntry(subQuery::getParameter).nonNullValues().toSortedMap());
        }
    }

    static class DebugQueryLogger
    {
        private String lastQuery;
        private final PrintStream ps;

        public DebugQueryLogger(PrintStream ps)
        {
            this.ps = ps;
        }

        public void log(String name, AstStart ast)
        {
            log(name, ast.format());
        }

        public void log(String name, String query)
        {
            if(!query.equals(lastQuery)) {
                ps.println(name+": ");
                if(lastQuery == null) {
                    ps.println(query);
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
                    ps.println(substring);
                }
                lastQuery = query;
            }
        }
    }

    @FunctionalInterface
    private interface VarResolver
    {
        String resolve(String varName);
    }

    private static class RootVarResolver implements VarResolver
    {

        private final DynamicPropertySet dps;

        public RootVarResolver(DynamicPropertySet dps)
        {
            this.dps = dps;
        }

        @Override
        public String resolve(String varName)
        {
            String value = dps.getValueAsString(varName);
            return value != null ? value : varName;
        }

    }

    private static class CompositeVarResolver implements VarResolver
    {

        private final VarResolver local;
        private final VarResolver parent;

        public CompositeVarResolver(VarResolver local, VarResolver parent)
        {
            this.local = local;
            this.parent = parent;
        }

        @Override
        public String resolve(String varName)
        {
            String value = local.resolve(varName);

            if (value != null)
                return value;

            return parent.resolve(varName);
        }

    }

    /**
     * Executes subqueries of the cell or returns the cell content itself.
     */
    @Override
    public Object formatCell(RawCellModel cell, DynamicPropertySet previousCells)
    {
        return new CellFormatter(cell, new RootVarResolver(previousCells)).format();
    }

    @Override
    public StreamEx<DynamicPropertySet> execute()
    {
        extraQuery = ExtraQuery.DEFAULT;
        return executeQuery();
    }

    @Override
    public StreamEx<DynamicPropertySet> executeAggregate(){
        extraQuery = ExtraQuery.AGGREGATE;
        return executeQuery();
    }

    /**
     * @throws Be5Exception
     */
    @Override
    public long count()
    {
        extraQuery = ExtraQuery.COUNT;
        try (StreamEx<DynamicPropertySet> stream = executeQuery())
        {
            DynamicPropertySet dynamicProperties = stream.findFirst().orElse(null);

            return (Long)dynamicProperties.asMap().get("count");
        }
    }

    @Override
    public DynamicPropertySet getRow()
    {
        try (StreamEx<DynamicPropertySet> stream = executeQuery())
        {
            return stream.findFirst().get();
        }
    }

}
