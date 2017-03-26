package com.developmentontheedge.be5.api.services;

import com.developmentontheedge.be5.api.sql.ResultSetParser;
import org.apache.commons.dbutils.ResultSetHandler;

import java.util.List;

public interface SqlService
{
    <T> T query(String sql, ResultSetHandler<T> rsh, Object... params);

    <T> T select(String sql, ResultSetParser<T> parser, Object... params);

    <T> List<T> selectList(String sql, ResultSetParser<T> parser, Object... params);

    <T> T selectScalar(String sql, Object... params);

    int update(String sql, Object... params);

    <T> T insert(String sql, Object... params);
}
