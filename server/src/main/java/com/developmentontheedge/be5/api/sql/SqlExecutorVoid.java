package com.developmentontheedge.be5.api.sql;

import java.sql.Connection;
import java.sql.SQLException;

@FunctionalInterface
public interface SqlExecutorVoid
{
    void run(Connection conn) throws SQLException;
}
