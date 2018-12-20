package com.developmentontheedge.be5.query.impl;

import com.developmentontheedge.be5.query.OrderedQueryExecutor;

import static com.google.common.base.Preconditions.checkArgument;


public abstract class AbstractOrderedQueryExecutor implements OrderedQueryExecutor
{
    protected int offset = 0;
    protected int limit = Integer.MAX_VALUE;

    protected int orderColumn = -1;
    protected String orderDir = "asc";

    @Override
    public final OrderedQueryExecutor offset(int offset)
    {
        checkArgument(offset >= 0);
        this.offset = offset;
        return this;
    }

    @Override
    public final OrderedQueryExecutor limit(int limit)
    {
        checkArgument(limit >= 0);
        this.limit = limit;
        return this;
    }

    @Override
    public final OrderedQueryExecutor order(int orderColumn, String orderDir)
    {
        checkArgument(orderColumn >= -2);
        this.orderColumn = orderColumn;
        this.orderDir = orderDir;
        return this;
    }

    @Override
    public int getOrderColumn()
    {
        return orderColumn;
    }

    @Override
    public String getOrderDir()
    {
        return orderDir;
    }

    @Override
    public int getOffset()
    {
        return offset;
    }

    @Override
    public int getLimit()
    {
        return limit;
    }
}
