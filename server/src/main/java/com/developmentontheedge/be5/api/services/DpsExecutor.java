package com.developmentontheedge.be5.api.services;

import com.developmentontheedge.be5.api.services.impl.DpsExecutorImpl;
import com.developmentontheedge.beans.DynamicPropertySet;
import one.util.streamex.StreamEx;

import java.util.List;


public interface DpsExecutor
{
    /**
     * List an SQL query result as a sequence of dynamic property sets.
     */
    List<DynamicPropertySet> list(String sql, DpsExecutorImpl.MetaProcessor metaProcessor);

    DynamicPropertySet get(String sql);

    DynamicPropertySet get(String sql, DpsExecutorImpl.MetaProcessor metaProcessor);

    /**
     * Streams an SQL query result as a sequence of dynamic property sets.
     */
    StreamEx<DynamicPropertySet> stream(String sql, DpsExecutorImpl.MetaProcessor metaProcessor);

}
