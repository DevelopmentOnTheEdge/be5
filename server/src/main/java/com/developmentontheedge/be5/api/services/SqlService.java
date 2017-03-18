package com.developmentontheedge.be5.api.services;

import org.apache.commons.dbutils.ResultSetHandler;

import java.util.List;

public interface SqlService
{
    public <T> T select(String sql, ResultSetHandler<T> rsh, Object... params);

    public <T> List<T> selectAll(String sql, ResultSetHandler<T> rsh, Object... params);

}
