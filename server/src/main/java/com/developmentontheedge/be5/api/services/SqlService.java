package com.developmentontheedge.be5.api.services;

import org.apache.commons.dbutils.ResultSetHandler;

import java.util.List;

public interface SqlService
{
    <T> T select(String sql, ResultSetHandler<T> rsh, Object... params);

    <T> List<T> selectAll(String sql, ResultSetHandler<T> rsh, Object... params);

    int update(String sql, Object... params);

    <T> T insert(String sql, ResultSetHandler<T> rsh, Object... params);
}
