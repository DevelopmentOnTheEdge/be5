package com.developmentontheedge.be5.api.services;

import com.developmentontheedge.be5.api.services.impl.DpsStreamerImpl;
import com.developmentontheedge.beans.DynamicProperty;
import com.developmentontheedge.beans.DynamicPropertySet;
import one.util.streamex.StreamEx;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;

public interface DpsStreamer
{
    /**
     * Streams an SQL query result as a sequence of dynamic property sets.
     */
    StreamEx<DynamicPropertySet> stream(String sql, DpsStreamerImpl.MetaProcessor metaProcessor);

    DynamicProperty[] createSchema(ResultSetMetaData metaData) throws SQLException;
}
