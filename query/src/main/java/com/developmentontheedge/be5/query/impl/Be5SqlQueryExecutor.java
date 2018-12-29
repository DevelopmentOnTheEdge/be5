package com.developmentontheedge.be5.query.impl;

import com.developmentontheedge.be5.base.services.Meta;
import com.developmentontheedge.be5.base.services.UserInfoProvider;
import com.developmentontheedge.be5.database.DbService;
import com.developmentontheedge.be5.metadata.QueryType;
import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.query.QueryConstants;
import com.developmentontheedge.be5.query.QueryExecutor;
import com.developmentontheedge.be5.query.QuerySession;
import com.developmentontheedge.be5.query.model.beans.QRec;
import com.developmentontheedge.be5.query.sql.QRecParser;
import com.developmentontheedge.be5.query.support.AbstractQueryExecutor;
import com.developmentontheedge.be5.query.util.DynamicPropertyMeta;
import com.developmentontheedge.be5.query.util.TableUtils;
import com.developmentontheedge.beans.DynamicProperty;
import com.developmentontheedge.beans.DynamicPropertySet;
import com.developmentontheedge.beans.DynamicPropertySetAsMap;
import com.developmentontheedge.sql.format.ContextApplier;
import com.developmentontheedge.sql.format.QueryContext;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.StreamSupport;

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

    public QueryExecutor initialize(Query query, Map<String, Object> parameters)
    {
        this.query = Objects.requireNonNull(query);
        this.parameters = Objects.requireNonNull(parameters);

        queryContext = new Be5QueryContext(query, parameters, querySession, userInfoProvider.get(), meta);
        contextApplier = new ContextApplier(queryContext);

        selectable = query.getType() == QueryType.D1 && query.getOperationNames().getFinalValues().stream()
                .map(name -> meta.getOperation(query.getEntity().getName(), name).getRecords())
                .filter(r -> r == 1 || r == 2).count() > 0;
        return this;
    }

    private List<QRec> list(ExecuteType executeType)
    {
        return db.list(querySqlGenerator.getSql(query, queryContext, executeType), new QRecParser());
    }

    @Override
    public List<QRec> execute()
    {
        List<QRec> rows = list(ExecuteType.DEFAULT);
        addAggregateRowIfNeeded(rows);
        return processRows(rows);
    }

    private List<QRec> processRows(List<QRec> rows)
    {
        List<QRec> res = new ArrayList<>();
        rows.forEach(cells -> res.add(processCells(cells)));
        return res;
    }

    private QRec processCells(DynamicPropertySet cells)
    {
        QRec resultCells = new QRec();
        DynamicPropertySet previousCells = new DynamicPropertySetAsMap();

        for (DynamicProperty cell : cells)
        {
            Object processedContent = cellFormatter.formatCell(cell, previousCells, query, contextApplier);
            DynamicProperty property = new DynamicProperty(cell.getName(), processedContent == null ? String.class
                    : processedContent.getClass(), processedContent);
            DynamicPropertyMeta.set(property, DynamicPropertyMeta.get(cell));
            previousCells.add(property);
            if (!cell.isHidden())
            {
                resultCells.add(property);
            }
        }

        return resultCells;
    }

    private void addAggregateRowIfNeeded(List<QRec> propertiesList)
    {
        if (propertiesList.size() > 0 && StreamSupport.stream(propertiesList.get(0).spliterator(), false)
                .anyMatch(x -> DynamicPropertyMeta.get(x).containsKey(QueryConstants.COL_ATTR_AGGREGATE)))
        {
            List<QRec> aggregateRows = list(ExecuteType.AGGREGATE);
            TableUtils.addAggregateRowIfNeeded(propertiesList, aggregateRows, queryMetaHelper.getTotalTitle(query));
        }
    }

    @Override
    public long count()
    {
        return (Long) list(ExecuteType.COUNT).get(0).asMap().get("count");
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

    enum ExecuteType
    {
        DEFAULT, COUNT, AGGREGATE
    }
}
