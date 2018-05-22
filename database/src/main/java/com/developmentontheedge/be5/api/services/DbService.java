package com.developmentontheedge.be5.api.services;

import com.developmentontheedge.be5.api.sql.ResultSetParser;
import com.developmentontheedge.be5.api.sql.SqlExecutor;
import com.developmentontheedge.be5.api.sql.SqlExecutorVoid;
import com.developmentontheedge.be5.api.sql.parsers.ScalarParser;
import org.apache.commons.dbutils.ResultSetHandler;

import java.util.List;


public interface DbService
{
    <T> T query(String sql, ResultSetHandler<T> rsh, Object... params);

    <T> T select(String sql, ResultSetParser<T> parser, Object... params);

    <T> List<T> list(String sql, ResultSetParser<T> parser, Object... params);

    <T> T one(String sql, Object... params);

    int update(String sql, Object... params);

    //todo add QueryRunner batch, insertBatch

    int updateWithoutBeSql(String sql, Object... params);

    <T> T insert(String sql, Object... params);

    <T> T execute(SqlExecutor<T> executor);

    <T> T transactionWithResult(SqlExecutor<T> executor);

    void transaction(SqlExecutorVoid executor);

    default Long oneLong(String sql, Object... params)
    {
        Object number = one(sql, params);
        if(number == null) return null;

        Long res;
        if(!(number instanceof Long))
        {
            res = Long.parseLong(number.toString());
        }
        else
        {
            res = (Long)number;
        }
        return res;
    }

    default String oneString(String sql, Object... params)
    {
        return one(sql, params);
    }

    default Integer oneInteger(String sql, Object... params)
    {
        return one(sql, params);
    }

    default <T> List<T> scalarList(String sql, Object... params)
    {
        return list(sql, new ScalarParser<T>(), params);
    }

    default Long[] longArray(String sql, Object... params)
    {
        List<Long> list = list(sql, new ScalarParser<Long>(), params);
        return list.toArray(new Long[list.size()]);
    }

    default String[] stringArray(String sql, Object... params)
    {
        List<String> list = list(sql, new ScalarParser<String>(), params);
        return list.toArray(new String[list.size()]);
    }

}
