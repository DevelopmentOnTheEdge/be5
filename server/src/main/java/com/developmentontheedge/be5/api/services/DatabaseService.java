package com.developmentontheedge.be5.api.services;

import com.developmentontheedge.be5.api.sql.SqlExecutor;
import com.developmentontheedge.be5.metadata.sql.Rdbms;
import com.developmentontheedge.dbms.DbmsConnector;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

public interface DatabaseService extends DbmsConnector
{
    Connection getConnection(boolean isReadOnly) throws SQLException;

    Connection getCurrentTxConn();

    <T> T transaction(SqlExecutor<T> executor);

    Rdbms getRdbms();

    String getConnectionProfileName();

    String getUsername();

    Map<String, String> getParameters();
}
