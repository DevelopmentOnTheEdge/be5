package com.developmentontheedge.be5.metadata.sql;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class ConnectionUrlTest
{
    @Test
    public void testSqlServer()
    {
        ConnectionUrl url = new ConnectionUrl("jdbc:sqlserver://server.example.com:1433;databaseName=mydb");
        assertEquals("server.example.com", url.getHost());
        assertEquals(1433, url.getPort());
        assertEquals("mydb", url.getDb());
        assertEquals(Rdbms.SQLSERVER, url.getRdbms());
        assertEquals("jdbc:sqlserver://server.example.com:1433;databaseName=mydb", url.toString());
        url.setDb("otherdb");
        assertEquals("jdbc:sqlserver://server.example.com:1433;databaseName=otherdb", url.toString());
        url.setPort(1444);
        assertEquals("jdbc:sqlserver://server.example.com:1444;databaseName=otherdb", url.toString());
    }

    @Test
    public void testSqlServerJtds()
    {
        ConnectionUrl url = new ConnectionUrl("jdbc:jtds:sqlserver://server.example.com:1433/mydb;useCursors=true");
        assertEquals("server.example.com", url.getHost());
        assertEquals(1433, url.getPort());
        assertEquals("mydb", url.getDb());
        assertEquals(Rdbms.SQLSERVER, url.getRdbms());
        assertEquals("true", url.getProperty("useCursors"));
        assertEquals("jdbc:jtds:sqlserver://server.example.com:1433/mydb;useCursors=true", url.toString());
    }

    @Test
    public void testOracleThin()
    {
        ConnectionUrl url = new ConnectionUrl("jdbc:oracle:thin:@server.example.com:1521:mydb");
        assertEquals("server.example.com", url.getHost());
        assertEquals(1521, url.getPort());
        assertNull(url.getDb());
        assertEquals("mydb", url.getProperty("SID"));
        assertEquals(Rdbms.ORACLE, url.getRdbms());
        assertEquals("jdbc:oracle:thin:@server.example.com:1521:mydb", url.toString());
    }

    @Test
    public void testDb2()
    {
        ConnectionUrl url = new ConnectionUrl("jdbc:db2://localhost:50000/MYDB");
        assertEquals("localhost", url.getHost());
        assertEquals(50000, url.getPort());
        assertEquals("MYDB", url.getDb());
        assertEquals(Rdbms.DB2, url.getRdbms());
        assertEquals("jdbc:db2://localhost:50000/MYDB", url.toString());
    }

    @Test
    public void testMySQL()
    {
        ConnectionUrl url = new ConnectionUrl("jdbc:mysql://localhost:3306/mydb?zeroDateTimeBehavior=convertToNull&useUnicode=true&characterEncoding=UTF-8");
        assertEquals("localhost", url.getHost());
        assertEquals(3306, url.getPort());
        assertEquals("mydb", url.getDb());
        assertEquals(Rdbms.MYSQL, url.getRdbms());
        assertEquals("convertToNull", url.getProperty("zeroDateTimeBehavior"));
        assertEquals("true", url.getProperty("useUnicode"));
        assertEquals("UTF-8", url.getProperty("characterEncoding"));

        // toString is url.createConnectionUrl( true ) meaning for Context - therefore &amp;
        assertEquals("jdbc:mysql://localhost:3306/mydb?characterEncoding=UTF-8&amp;useUnicode=true&amp;zeroDateTimeBehavior=convertToNull", url.toString());

        assertEquals("jdbc:mysql://localhost:3306/mydb?characterEncoding=UTF-8&useUnicode=true&zeroDateTimeBehavior=convertToNull",
                url.createConnectionUrl(false));
    }
}
