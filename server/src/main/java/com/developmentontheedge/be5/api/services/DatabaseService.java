package com.developmentontheedge.be5.api.services;

import com.developmentontheedge.be5.api.exceptions.Be5Exception;
import com.developmentontheedge.be5.api.sql.SqlExecutor;
import com.developmentontheedge.be5.metadata.sql.Rdbms;
import com.developmentontheedge.dbms.DbmsType;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

public interface DatabaseService
{
    String getConnectString();

    Connection getConnection(boolean isReadOnly) throws SQLException;

    Connection getCurrentTxConn();

    <T> T transaction(SqlExecutor<T> executor);

    void releaseConnection( Connection conn );

    Be5Exception rollback(Connection conn, Throwable e);

    Rdbms getRdbms();

    String getConnectionProfileName();

    String getUsername();

    Map<String, String> getParameters();
}
