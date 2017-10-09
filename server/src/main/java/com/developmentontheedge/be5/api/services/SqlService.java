package com.developmentontheedge.be5.api.services;

import com.developmentontheedge.be5.api.sql.ResultSetParser;
import org.apache.commons.dbutils.ResultSetHandler;

import java.util.List;

public interface SqlService
{
    <T> T query(String sql, ResultSetHandler<T> rsh, Object... params);

    <T> T select(String sql, ResultSetParser<T> parser, Object... params);

    <T> List<T> selectList(String sql, ResultSetParser<T> parser, Object... params);

    <T> T getScalar(String sql, Object... params);

    int update(String sql, Object... params);

    //todo add несколько запросов через ';',
    //todo add QueryRunner batch, insertBatch

    int updateWithoutBeSql(String sql, Object... params);

    <T> T insert(String sql, Object... params);

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

    default String getString(String sql, Object... params){
        return getScalar(sql, params);
    }

    default Integer getInteger(String sql, Object... params){
        return getScalar(sql, params);
    }

    default Double getDouble(String sql, Object... params){
        return getScalar(sql, params);
    }
}
