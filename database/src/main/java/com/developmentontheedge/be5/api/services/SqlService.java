package com.developmentontheedge.be5.api.services;

import com.developmentontheedge.be5.api.sql.ResultSetParser;
import com.developmentontheedge.be5.api.sql.SqlExecutor;
import com.developmentontheedge.be5.api.sql.SqlExecutorVoid;
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

    default List<String> selectListString(String sql, Object... params)
    {
        return selectList(sql, rs-> rs.getString(1), params);
    }

    default String[] selectArrayString(String sql, Object... params)
    {
        List<String> strings = selectList(sql, rs -> rs.getString(1), params);

        String[] stockArr = new String[strings.size()];
        stockArr = strings.toArray(stockArr);
        return stockArr;
    }

    default Long[] selectArrayLong(String sql, Object... params)
    {
        List<Long> longs = selectList(sql, rs -> rs.getLong(1), params);

        Long[] stockArr = new Long[longs.size()];
        stockArr = longs.toArray(stockArr);
        return stockArr;
    }

    default List<Integer> selectListInteger(String sql, Object... params)
    {
        return selectList(sql, rs-> rs.getInt(1), params);
    }

    default List<Long> selectListLong(String sql, Object... params)
    {
        return selectList(sql, rs-> rs.getLong(1), params);
    }

    default List<Double> selectListDouble(String sql, Object... params)
    {
        return selectList(sql, rs-> rs.getDouble(1), params);
    }

    default List<Date> selectListDate(String sql, Object... params)
    {
        return selectList(sql, rs-> rs.getDate(1), params);
    }

}
