package com.developmentontheedge.be5.api.services;

import com.developmentontheedge.be5.api.services.impl.DpsExecutorImpl;
import com.developmentontheedge.beans.DynamicProperty;
import com.developmentontheedge.beans.DynamicPropertySet;
import one.util.streamex.StreamEx;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;

public interface DpsExecutor
{
    /**
     * List an SQL query result as a sequence of dynamic property sets.
     */
    List<DynamicPropertySet> list(String sql, DpsExecutorImpl.MetaProcessor metaProcessor);

    DynamicPropertySet getDps(ResultSet resultSet);

    DynamicPropertySet getDps(ResultSet resultSet, DpsExecutorImpl.MetaProcessor metaProcessor);
    /**
     * Streams an SQL query result as a sequence of dynamic property sets.
     */
    StreamEx<DynamicPropertySet> stream(String sql, DpsExecutorImpl.MetaProcessor metaProcessor);

    DynamicProperty[] createSchema(ResultSetMetaData metaData) throws SQLException;
}
