package com.developmentontheedge.be5.test.mocks;

import com.developmentontheedge.be5.api.services.DatabaseService;
import com.developmentontheedge.sql.format.Dbms;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class DatabaseServiceMock implements DatabaseService
{
    @Override
    public Connection getConnection() throws SQLException
    {
        return null;
    }

    @Override
    public Dbms getDbms() {
        return Dbms.H2;
    }

    @Override
    public Map<String, String> getParameters() {
        return new HashMap<>();
    }

//
//    @Override
//    public int executeUpdate(String query) throws SQLException {
//        return 0;
//    }
//
//    @Override
//    public ResultSet executeQuery(String sql) throws SQLException {
//        return null;
//    }
//
//    @Override
//    public String executeInsert(String sql) throws SQLException {
//        return "";
//    }
//
//    @Override
//    public void close(ResultSet rs) {
//
//    }
//
//    @Override
//    public Connection getConnection() throws SQLException {
//        return null;
//    }

}
