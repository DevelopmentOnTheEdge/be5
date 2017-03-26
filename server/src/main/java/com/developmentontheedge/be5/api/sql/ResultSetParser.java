package com.developmentontheedge.be5.api.sql;

import java.sql.ResultSet;
import java.sql.SQLException;

@FunctionalInterface
public interface ResultSetParser<T>
{
    ThreadLocal<ResultSetDelegator> CACHE = new ThreadLocal<>();

    default T parse(ResultSet rs) throws SQLException
    {
        ResultSetDelegator resultSetDelegator = CACHE.get();
        if(resultSetDelegator == null){
            resultSetDelegator = new ResultSetDelegator(rs);
            CACHE.set(resultSetDelegator);
        }else{
            resultSetDelegator.setResultSet(rs);
        }
        return parse(resultSetDelegator);
    }

    T parse(ResultSetDelegator rs) throws SQLException;
}