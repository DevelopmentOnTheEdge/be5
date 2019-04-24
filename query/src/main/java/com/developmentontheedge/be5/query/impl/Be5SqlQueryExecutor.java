package com.developmentontheedge.be5.query.impl;

import com.developmentontheedge.be5.database.DbService;
import com.developmentontheedge.be5.meta.Meta;
import com.developmentontheedge.be5.metadata.QueryType;
import com.developmentontheedge.be5.metadata.model.Operation;
import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.query.QueryConstants;
import com.developmentontheedge.be5.query.QueryExecutor;
import com.developmentontheedge.be5.query.QuerySession;
import com.developmentontheedge.be5.query.model.beans.QRec;
import com.developmentontheedge.be5.query.sql.QRecParser;
import com.developmentontheedge.be5.query.support.AbstractQueryExecutor;
import com.developmentontheedge.be5.query.util.AggregateUtils;
import com.developmentontheedge.be5.query.util.DynamicPropertyMeta;
import com.developmentontheedge.be5.query.util.QueryUtils;
import com.developmentontheedge.be5.security.UserInfoProvider;
import com.developmentontheedge.beans.DynamicPropertySet;
import com.developmentontheedge.sql.format.ContextApplier;
import com.developmentontheedge.sql.format.QueryContext;
import com.developmentontheedge.sql.model.AstStart;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.developmentontheedge.be5.query.QueryConstants.LIMIT;
import static com.developmentontheedge.be5.query.QueryConstants.OFFSET;
import static com.developmentontheedge.be5.query.QueryConstants.ORDER_COLUMN;
import static com.developmentontheedge.be5.query.QueryConstants.ORDER_DIR;

/**
 * A modern query executor that uses our new parser.
 */
public class Be5SqlQueryExecutor extends AbstractQueryExecutor implements QueryExecutor
{
    private DbService db;
    private Meta meta;
    private QuerySession querySession;
    private UserInfoProvider userInfoProvider;
    private QueryMetaHelper queryMetaHelper;
    private CellFormatter cellFormatter;
    private QuerySqlGenerator querySqlGenerator;

    private Query query;
    private Map<String, ?> parameters;
    private ContextApplier contextApplier;
    private QueryContext queryContext;
    private Boolean selectable;

    @Inject
    public void inject(QuerySession querySession, UserInfoProvider userInfoProvider, Meta meta, DbService db,
                       QueryMetaHelper queryMetaHelper, CellFormatter cellFormatter,
                       QuerySqlGenerator querySqlGenerator)
    {
        this.querySession = querySession;
        this.userInfoProvider = userInfoProvider;
        this.meta = meta;
        this.db = db;
        this.queryMetaHelper = queryMetaHelper;
        this.cellFormatter = cellFormatter;
        this.querySqlGenerator = querySqlGenerator;
    }

    public void initialize(Query query, Map<String, Object> parameters)
    {
        this.query = Objects.requireNonNull(query);
        this.parameters = Objects.requireNonNull(parameters);

        queryContext = new Be5QueryContext(query, parameters, querySession, userInfoProvider.getLoggedUser(), meta);
        contextApplier = new ContextApplier(queryContext);

        selectable = query.getType() == QueryType.D1 && query.getOperationNames().getFinalValues().stream()
                .map(name -> meta.getOperation(query.getEntity().getName(), name))
                .filter(o -> meta.hasAccess(o.getRoles(), userInfoProvider.getCurrentRoles()))
                .map(Operation::getRecords).anyMatch(r -> r == 1 || r == 2);
    }

    @Override
    public List<QRec> execute()
    {
        List<QRec> rows = db.list(querySqlGenerator.getSql(query, queryContext), new QRecParser());
        addAggregateRowIfNeeded(rows);
        return rows.stream()
                .map(this::formatCells)
                .collect(Collectors.toList());
    }

    private QRec formatCells(DynamicPropertySet properties)
    {
        return cellFormatter.toRow(properties, (name) -> null, query, contextApplier);
    }

    private void addAggregateRowIfNeeded(List<QRec> rows)
    {
        if (rows.size() > 0 && StreamSupport.stream(rows.get(0).spliterator(), false)
                .anyMatch(x -> DynamicPropertyMeta.get(x).containsKey(QueryConstants.COL_ATTR_AGGREGATE)))
        {
            AstStart sql = querySqlGenerator.getSql(query, getParamsWithoutLimit());
            List<QRec> aggregateRows = db.list(sql, new QRecParser());
            AggregateUtils.addAggregateRow(rows, aggregateRows, queryMetaHelper.getTotalTitle(query));
        }
    }

    @Override
    public long count()
    {
        AstStart sql = querySqlGenerator.getSql(query, getParamsWithoutLimit());
        QueryUtils.countFromQuery(sql.getQuery());
        return db.countFrom(sql.format());
    }

    private Map<String, Object> getParamsWithoutLimit()
    {
        return new HashMap<String, Object>(parameters) {{
            remove(LIMIT); remove(OFFSET);
            remove(ORDER_COLUMN); remove(ORDER_DIR);
        }};
    }

    @Override
    public Boolean isSelectable()
    {
        return selectable;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> getParameters()
    {
        return (Map<String, Object>) parameters;
    }
}
