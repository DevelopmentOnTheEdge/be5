package com.developmentontheedge.be5.api.services;

import com.developmentontheedge.sql.format.Dbms;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;


public interface DatabaseService
{
    String getConnectString();

    Connection getConnection() throws SQLException;

    Dbms getDbms();

    Map<String, String> getParameters();
}
