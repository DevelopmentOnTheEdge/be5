package com.developmentontheedge.be5.query.impl.model;

import java.util.Objects;

import com.developmentontheedge.be5.api.services.QueryExecutor;
import com.developmentontheedge.be5.metadata.model.Query;

import static com.google.common.base.Preconditions.checkArgument;


public abstract class AbstractQueryExecutor implements QueryExecutor
{
    protected final Query query;
    
    protected int offset = 0;
    protected int limit = Integer.MAX_VALUE;
    protected int orderColumn = -1;
    protected boolean orderDesc = false;
    
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
    public final QueryExecutor order(int column, boolean desc)
    {
        checkArgument( column >= -2 );
        this.orderColumn = column;
        this.orderDesc = desc;
        return this;
    }

    public abstract String getFinalSql();
}
