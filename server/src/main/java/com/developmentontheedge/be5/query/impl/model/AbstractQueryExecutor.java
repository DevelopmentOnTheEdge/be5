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
    private int orderColumn = -1;
    protected boolean orderDesc = false;
    protected Boolean selectable;
    
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
    public final QueryExecutor order(int orderColumn, boolean desc)
    {
        checkArgument( orderColumn >= -2 );
        this.orderColumn = orderColumn;
        this.orderDesc = desc;
        return this;
    }

    @Override
    public void setSelectable(boolean selectable)
    {
        this.selectable = selectable;
    }

    @Override
    public int getOrderColumn()
    {
        return orderColumn + (selectable ? -1 : 0);
    }

    @Override
    public Boolean getSelectable()
    {
        return selectable;
    }

    public abstract String getFinalSql();
}