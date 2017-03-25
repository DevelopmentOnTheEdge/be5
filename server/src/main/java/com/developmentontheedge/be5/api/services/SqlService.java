package com.developmentontheedge.be5.api.services;

import com.developmentontheedge.be5.api.sql.ResultSetParser;
import org.apache.commons.dbutils.ResultSetHandler;

import java.util.List;

public interface SqlService
{
    <T> T query(String sql, ResultSetHandler<T> rsh, Object... params);

    <T> T select(String sql, ResultSetParser<T> rsh, Object... params);

    <T> List<T> selectList(String sql, ResultSetParser<T> rsh, Object... params);

    Long selectLong(String sql, Object... params);

    String selectString(String sql, Object... params);

    int update(String sql, Object... params);

    <T> T insert(String sql, Object... params);
}
