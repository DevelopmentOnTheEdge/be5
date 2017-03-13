package com.developmentontheedge.dbms;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

public interface DbmsConnector
{
    public DbmsType getType();

    public String getConnectString();

    public int executeUpdate( String query ) throws SQLException;

    public ResultSet executeQuery( String sql ) throws SQLException;

    public String executeInsert( String sql ) throws SQLException;

    public void close( ResultSet rs );

    public Connection getConnection() throws SQLException;

    public void releaseConnection( Connection conn ) throws SQLException;

    boolean isMySQL();
    boolean isMySQL5();
    boolean isMySQL41();

    boolean isODBC();

    boolean isSQLite();

    boolean isOracle();
    boolean isOracle8();

    boolean isSQLServer();
    boolean isSQLServer2005();
    boolean isSQLServerJTDS();

    boolean isDb2();
    boolean isDb2NetDriver();
    boolean isDb2AppDriver();
    boolean isDb2v8();
    boolean isDb2v9();

    boolean isPostgreSQL();

    boolean isH2();
}
