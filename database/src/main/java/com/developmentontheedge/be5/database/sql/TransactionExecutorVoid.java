package com.developmentontheedge.be5.database.sql;

import java.sql.Connection;

@FunctionalInterface
public interface TransactionExecutorVoid
{
    void run(Connection conn) throws Throwable;
}
