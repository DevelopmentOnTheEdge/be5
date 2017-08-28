package com.developmentontheedge.be5.test.mocks;

import com.developmentontheedge.be5.api.services.DatabaseService;
import com.developmentontheedge.be5.api.sql.SqlExecutor;
import com.developmentontheedge.be5.metadata.sql.Rdbms;
import com.developmentontheedge.dbms.DbmsType;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class DatabaseServiceMock implements DatabaseService
{
    @Override
    public Connection getConnection(boolean isReadOnly) throws SQLException {
        return null;
    }

    @Override
    public Connection getCurrentTxConn() {
        return null;
    }

    @Override
    public <T> T transaction(SqlExecutor<T> executor) {
        try {
            executor.run(null);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public int getNumIdle() {
        return 0;
    }

    @Override
    public int getNumActive() {
        return 0;
    }

    @Override
    public String getConnectionsStatistics() {
        return "test";
    }

    @Override
    public Rdbms getRdbms() {
        return Rdbms.POSTGRESQL;
    }

    @Override
    public String getConnectionProfileName() {
        return "test";
    }

    @Override
    public String getUsername() {
        return "test";
    }

    @Override
    public Map<String, String> getParameters() {
        return new HashMap<>();
    }

    @Override
    public DbmsType getType() {
        return getRdbms().getType();
    }

    @Override
    public String getConnectString() {
        return "jdbc:postgresql://localhost:5432/test";
    }

    @Override
    public int executeUpdate(String query) throws SQLException {
        return 0;
    }

    @Override
    public ResultSet executeQuery(String sql) throws SQLException {
        return null;
    }

    @Override
    public String executeInsert(String sql) throws SQLException {
        return "";
    }

    @Override
    public void close(ResultSet rs) {

    }

    @Override
    public Connection getConnection() throws SQLException {
        return null;
    }

    @Override
    public void releaseConnection(Connection conn) {

    }
}
