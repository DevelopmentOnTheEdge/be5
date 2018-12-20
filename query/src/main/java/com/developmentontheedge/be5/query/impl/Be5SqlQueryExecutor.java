package com.developmentontheedge.be5.query.impl;

import com.developmentontheedge.be5.base.exceptions.Be5Exception;
import com.developmentontheedge.be5.base.services.Meta;
import com.developmentontheedge.be5.database.DbService;
import com.developmentontheedge.be5.database.sql.ResultSetParser;
import com.developmentontheedge.be5.metadata.QueryType;
import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.query.QueryConstants;
import com.developmentontheedge.be5.query.SqlQueryExecutor;
import com.developmentontheedge.be5.query.sql.DynamicPropertySetSimpleStringParser;
import com.developmentontheedge.be5.query.support.AbstractQueryExecutor;
import com.developmentontheedge.be5.query.util.DynamicPropertyMeta;
import com.developmentontheedge.be5.query.util.TableUtils;
import com.developmentontheedge.beans.DynamicPropertySet;
import com.developmentontheedge.sql.format.ContextApplier;
import com.developmentontheedge.sql.format.LimitsApplier;
import com.developmentontheedge.sql.format.MacroExpander;
import com.developmentontheedge.sql.format.QueryContext;
import com.developmentontheedge.sql.format.Simplifier;
import com.developmentontheedge.sql.model.AstStart;
import com.developmentontheedge.sql.model.SqlQuery;

import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.StreamSupport;

import static com.developmentontheedge.be5.base.FrontendConstants.CATEGORY_ID_PARAM;


/**
 * A modern query executor that uses our new parser.
 */
public class Be5SqlQueryExecutor extends AbstractQueryExecutor implements SqlQueryExecutor
{
    protected final Query query;

    private static final Logger log = Logger.getLogger(Be5SqlQueryExecutor.class.getName());

    private final DbService db;

    private final Map<String, List<Object>> parameters;
    private final QueryMetaHelper queryMetaHelper;

    private ContextApplier contextApplier;
    private final Boolean selectable;

    public Be5SqlQueryExecutor(Query query, QueryContext queryContext, Meta meta, DbService db,
                               QueryMetaHelper queryMetaHelper)
    {
        this.query = Objects.requireNonNull(query);

        this.parameters = queryContext.getParameters();

        this.db = db;
        this.queryMetaHelper = queryMetaHelper;

        this.contextApplier = new ContextApplier(queryContext);

        selectable = query.getType() == QueryType.D1 && query.getOperationNames().getFinalValues().stream()
                .map(name -> meta.getOperation(query.getEntity().getName(), name).getRecords())
                .filter(r -> r == 1 || r == 2).count() > 0;
    }

    @Override
    public <T> List<T> execute(ResultSetParser<T> parser)
    {
        return execute(ExecuteType.DEFAULT, parser);
    }

    private <T> List<T> execute(ExecuteType executeType, ResultSetParser<T> parser)
    {
        if (query.getType().equals(QueryType.D1) || query.getType().equals(QueryType.D1_UNKNOWN))
        {
            try
            {
                return db.list(getFinalSql(executeType), parser);
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
        return getFinalSql(ExecuteType.DEFAULT);
    }

    AstStart getFinalSql(ExecuteType executeType)
    {
        String queryText = query.getFinalQuery();
        if (queryText.isEmpty()) return null;

        AstStart ast = parseQuery(queryText);
        new MacroExpander().expandMacros(ast);

        queryMetaHelper.resolveTypeOfRefColumn(ast);
        queryMetaHelper.applyFilters(ast, parameters);
        QueryMetaHelper.applyCategory(query, ast, contextApplier.getContext().getParameter(CATEGORY_ID_PARAM));

        contextApplier.applyContext(ast);

        if (query.getType() == QueryType.D1) QueryMetaHelper.addIDColumnLabel(ast, query);

        if (executeType == ExecuteType.COUNT)
        {
            QueryMetaHelper.countFromQuery(ast.getQuery());
        }

        if (executeType == ExecuteType.DEFAULT)
        {
            QueryMetaHelper.applySort(ast, orderColumn + (selectable ? -1 : 0), orderDir);
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
        List<DynamicPropertySet> rows = execute(new DynamicPropertySetSimpleStringParser());
        addAggregateRowIfNeeded(rows);
        return rows;
    }

    private void addAggregateRowIfNeeded(List<DynamicPropertySet> propertiesList)
    {
        if (propertiesList.size() > 0 && StreamSupport.stream(propertiesList.get(0).spliterator(), false)
                .anyMatch(x -> DynamicPropertyMeta.get(x).containsKey(QueryConstants.COL_ATTR_AGGREGATE)))
        {

            List<DynamicPropertySet> aggregateRows = execute(ExecuteType.AGGREGATE, new DynamicPropertySetSimpleStringParser());
            TableUtils.addAggregateRowIfNeeded(propertiesList, aggregateRows, queryMetaHelper.getTotalTitle(query));
        }
    }

    @Override
    public long count()
    {
        return (Long) execute(ExecuteType.COUNT, new DynamicPropertySetSimpleStringParser()).get(0).asMap().get("count");
    }

    @Override
    public DynamicPropertySet getRow()
    {
        return getRow(new DynamicPropertySetSimpleStringParser());
    }

    @Override
    public Boolean isSelectable()
    {
        return selectable;
    }

    enum ExecuteType
    {
        DEFAULT, COUNT, AGGREGATE
    }
}
