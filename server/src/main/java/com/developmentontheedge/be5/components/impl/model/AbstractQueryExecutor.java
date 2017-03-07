package com.developmentontheedge.be5.components.impl.model;

import java.util.Objects;
import java.util.Optional;

import one.util.streamex.StreamEx;

import com.developmentontheedge.beans.DynamicPropertySet;
import com.developmentontheedge.be5.api.exceptions.Be5Exception;
import com.developmentontheedge.be5.api.services.QueryExecutor;
import com.developmentontheedge.be5.metadata.model.Query;

/**
 * 
 * @pending preconditions
 */
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
        //checkArgument( offset >= 0 );
        this.offset = offset;
        return this;
    }

    @Override
    public final QueryExecutor limit(int limit)
    {
        //checkArgument( limit >= 0 );
        this.limit = limit;
        return this;
    }

    @Override
    public final QueryExecutor sortOrder(int sortColumn, boolean desc)
    {
        //checkArgument( sortColumn >= -2 );
        this.sortColumn = sortColumn;
        this.sortDesc = desc;
        return this;
    }
    
    @Override
    public final boolean test() throws Be5Exception
    {
        Optional<DynamicPropertySet> resultRow = execute().findFirst();
        if (!resultRow.isPresent())
            Be5Exception.internalInQuery(new RuntimeException(), query);
        Object value = resultRow.get().getValue("value");
        if (value.getClass() != Long.class)
            Be5Exception.internalInQuery(new RuntimeException(), query);
        long resultValue = (Long) value;
        
        return resultValue == 1;
    }

}
