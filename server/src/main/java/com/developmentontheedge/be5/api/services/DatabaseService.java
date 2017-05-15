package com.developmentontheedge.be5.api.services;

import com.developmentontheedge.be5.api.operationstest.analyzers.DatabaseAnalyzer;
import com.developmentontheedge.be5.api.sql.SqlExecutor;
import com.developmentontheedge.be5.metadata.sql.Rdbms;
import com.developmentontheedge.dbms.DbmsConnector;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

public interface DatabaseService extends DbmsConnector
{
    Connection getConnection(boolean isReadOnly) throws SQLException;

    Connection getCurrentTxConn();

    <T> T transaction(SqlExecutor<T> executor);

    int getNumIdle();

    int getNumActive();

    String getConnectionsStatistics();

    Rdbms getRdbms();

    DatabaseAnalyzer getAnalyzer();

    String getConnectionProfileName();

    String getUsername();
}
