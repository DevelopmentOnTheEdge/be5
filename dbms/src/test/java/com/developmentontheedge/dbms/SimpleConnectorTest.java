package com.developmentontheedge.dbms;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.zapodot.junit.db.EmbeddedDatabaseRule;

import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.Assert.assertEquals;
import static org.zapodot.junit.db.EmbeddedDatabaseRule.CompatibilityMode.PostgreSQL;

public class SimpleConnectorTest
{
    @Rule
    public final EmbeddedDatabaseRule databaseRule = EmbeddedDatabaseRule.builder()
            .withMode(PostgreSQL)
            .withInitialSql("CREATE TABLE persons ( id BIGSERIAL PRIMARY KEY, name VARCHAR(255) NOT NULL, password VARCHAR(255) NOT NULL, email VARCHAR(255), age INT);")
            .build();

    SimpleConnector connector;

    @Before
    public void setUp() throws Exception
    {
        connector = new SimpleConnector(DbmsType.H2, databaseRule.getConnectionJdbcUrl(),
                databaseRule.getConnection());
    }

    @Test
    public void test() throws SQLException
    {
        ResultSet resultSet = connector.executeQuery("select count(1) from persons");
        resultSet.next();
        assertEquals(0L, resultSet.getLong(1));

        connector.close(resultSet);
    }
}