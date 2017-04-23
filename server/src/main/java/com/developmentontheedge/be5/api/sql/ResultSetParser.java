package com.developmentontheedge.be5.api.sql;

import java.sql.ResultSet;
import java.sql.SQLException;

@FunctionalInterface
public interface ResultSetParser<T>
{
    default T parse(ResultSet rs) throws SQLException
    {
        return parse(new ResultSetDelegator(rs));
    }

    T parse(ResultSetDelegator rs) throws SQLException;
}