package com.developmentontheedge.be5.query.support;

import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.query.QueryExecutor;

import java.util.Map;

import static com.developmentontheedge.be5.query.QueryConstants.LIMIT;
import static com.developmentontheedge.be5.query.QueryConstants.OFFSET;
import static com.developmentontheedge.be5.query.QueryConstants.ORDER_COLUMN;
import static com.developmentontheedge.be5.query.QueryConstants.ORDER_DIR;


public abstract class AbstractQueryExecutor implements QueryExecutor
{
    public abstract QueryExecutor initialize(Query query, Map<String, Object> parameters);

    @Override
    public int getOrderColumn()
    {
        return Integer.parseInt((String) getParameters().getOrDefault(ORDER_COLUMN, "-1"));
    }

    @Override
    public String getOrderDir()
    {
        return (String) getParameters().getOrDefault(ORDER_DIR, "asc");
    }

    @Override
    public int getOffset()
    {
        return Integer.parseInt((String) getParameters().getOrDefault(OFFSET, "0"));
    }

    @Override
    public int getLimit()
    {
        return Integer.parseInt((String) getParameters().getOrDefault(LIMIT, Integer.toString(Integer.MAX_VALUE)));
    }
}
