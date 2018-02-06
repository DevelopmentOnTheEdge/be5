package com.developmentontheedge.be5.components.impl.model;

import java.util.Objects;

import com.developmentontheedge.be5.api.services.QueryExecutor;
import com.developmentontheedge.be5.metadata.model.Query;

import static com.google.common.base.Preconditions.checkArgument;

public abstract class AbstractQueryExecutor implements QueryExecutor
{
    protected final Query query;
    
    protected int offset = 0;
    protected int limit = Integer.MAX_VALUE;
    protected int sortColumn;
    protected boolean sortDesc;
    
    public AbstractQueryExecutor(Query query)
    {
        this.query = Objects.requireNonNull( query );
    }

    @Override
    public final QueryExecutor offset(int offset)
    {
        checkArgument(offset >= 0);
        this.offset = offset;
        return this;
    }

    @Override
    public final QueryExecutor limit(int limit)
    {
        checkArgument( limit >= 0 );
        this.limit = limit;
        return this;
    }

    @Override
    public final QueryExecutor sortOrder(int sortColumn, boolean desc)
    {
        checkArgument( sortColumn >= -2 );
        this.sortColumn = sortColumn;
        this.sortDesc = desc;
        return this;
    }

    public abstract String getFinalSql();
}
