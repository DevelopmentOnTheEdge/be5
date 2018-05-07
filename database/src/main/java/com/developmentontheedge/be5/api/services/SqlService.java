package com.developmentontheedge.be5.api.services;

import com.developmentontheedge.be5.api.sql.ResultSetParser;
import com.developmentontheedge.be5.api.sql.SqlExecutor;
import com.developmentontheedge.be5.api.sql.SqlExecutorVoid;
import com.developmentontheedge.be5.api.sql.parsers.ScalarParser;
import org.apache.commons.dbutils.ResultSetHandler;

import java.sql.Date;
import java.util.List;


public interface SqlService
{
    <T> T query(String sql, ResultSetHandler<T> rsh, Object... params);

    <T> T select(String sql, ResultSetParser<T> parser, Object... params);

    <T> List<T> selectList(String sql, ResultSetParser<T> parser, Object... params);

    <T> T getScalar(String sql, Object... params);

    int update(String sql, Object... params);

    //todo add QueryRunner batch, insertBatch

    int updateWithoutBeSql(String sql, Object... params);

    <T> T insert(String sql, Object... params);

    <T> T transactionWithResult(SqlExecutor<T> executor);

    void transaction(SqlExecutorVoid executor);

    default Long getLong(String sql, Object... params)
    {
        Object number = getScalar(sql, params);
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

    default String getString(String sql, Object... params)
    {
        return getScalar(sql, params);
    }

    default Integer getInteger(String sql, Object... params)
    {
        return getScalar(sql, params);
    }

    default Double getDouble(String sql, Object... params)
    {
        return getScalar(sql, params);
    }

    default Date getDate(String sql, Object... params)
    {
        return getScalar(sql, params);
    }

    default <T> List<T> selectScalarList(String sql, Object... params)
    {
        return selectList(sql, new ScalarParser<T>(), params);
    }

    default Long[] selectLongArray(String sql, Object... params)
    {
        List<Long> list = selectList(sql, new ScalarParser<Long>(), params);
        return list.toArray(new Long[list.size()]);
    }

    default String[] selectStringArray(String sql, Object... params)
    {
        List<String> list = selectList(sql, new ScalarParser<String>(), params);
        return list.toArray(new String[list.size()]);
    }

}
