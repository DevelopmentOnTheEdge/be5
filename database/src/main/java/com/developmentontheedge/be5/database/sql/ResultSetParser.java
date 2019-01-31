package com.developmentontheedge.be5.database.sql;

import java.sql.ResultSet;
import java.sql.SQLException;

@FunctionalInterface
public interface ResultSetParser<T>
{
    T parse(ResultSet rs) throws SQLException;
}
