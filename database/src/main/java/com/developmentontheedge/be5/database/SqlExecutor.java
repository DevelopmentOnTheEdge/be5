package com.developmentontheedge.be5.database;

import java.sql.Connection;
import java.sql.SQLException;

@FunctionalInterface
public interface SqlExecutor<T>
{
    T run(Connection conn) throws SQLException;
}
