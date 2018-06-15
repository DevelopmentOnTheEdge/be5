package com.developmentontheedge.dbms;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.zapodot.junit.db.EmbeddedDatabaseRule;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.zapodot.junit.db.EmbeddedDatabaseRule.CompatibilityMode.PostgreSQL;

public class SqlExecutorTest
{
    @Rule
    public final EmbeddedDatabaseRule databaseRule = EmbeddedDatabaseRule.builder()
            .withMode(PostgreSQL)
            .withInitialSql("CREATE TABLE persons ( id BIGSERIAL PRIMARY KEY, name VARCHAR(255) NOT NULL, password VARCHAR(255) NOT NULL, email VARCHAR(255), age INT);")
            .build();

    private SqlExecutor sqlExecutor;
    private DbmsConnector connector;

    PrintStream psOut;

    @Before
    public void setUp() throws Exception
    {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        psOut = new PrintStream( out, true, "UTF-8" );

        connector = new SimpleConnector(DbmsType.H2, databaseRule.getConnectionJdbcUrl(),
                databaseRule.getDataSource());

        sqlExecutor = new SqlExecutor(connector, psOut, SqlExecutor.getDefaultPropertiesFile());

        sqlExecutor.testConnection();

        connector.executeUpdate("DELETE FROM persons");
        assertTrue(sqlExecutor.isEmpty("persons"));
    }

    @Test
    public void executeMultiple() throws ExtendedSqlException
    {
        sqlExecutor.startSection( "Sync schema" );

        String sql = "INSERT INTO persons (name, password) VALUES ('test','test');" +
                "INSERT INTO persons (name, password) VALUES ('test2','test');" +
                "INSERT INTO persons (name, password) VALUES ('test3','test')";
        MultiSqlParser.normalize(DbmsType.H2, sql);
        sqlExecutor.executeMultiple(sql);

        assertEquals("3", sqlExecutor.readString("sql.countRows", "persons"));
    }

    @Test
    public void execPrepared() throws ExtendedSqlException, IOException
    {
        SqlExecutor sqlTestExecutor = new SqlExecutor(connector, psOut, SqlExecutorTest.class.getResource( "test.properties" ));
        String s = sqlTestExecutor.execPrepared("sql.insert.person", new Object[]{"test", "test"}, true);

        assertEquals(1, sqlExecutor.count("persons"));
    }

    @Test
    public void exec() throws ExtendedSqlException, IOException, SQLException
    {
        SqlExecutor sqlTestExecutor = new SqlExecutor(connector, psOut, SqlExecutorTest.class.getResource( "test.properties" ));
        sqlTestExecutor.startSection( "exec test" );
        sqlTestExecutor.comment("test comment");
        sqlTestExecutor.exec("sql.insert.person", "'test'", "'test'");

        assertEquals(1, sqlExecutor.count("persons"));

        ResultSet rs = sqlExecutor.executeNamedQuery("sql.test.table", "persons");

        rs.next();
        assertEquals(1L, rs.getLong(1));

        sqlExecutor.close(rs);
    }
}