package com.developmentontheedge.dbms;

import org.junit.Test;

import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.Assert.assertEquals;

public class SimpleConnectorTest extends BaseDbmsTests
{
    @Test
    public void test() throws SQLException
    {
        ResultSet resultSet = connector.executeQuery("select count(1) from persons");
        resultSet.next();
        assertEquals(0L, resultSet.getLong(1));

        connector.close(resultSet);
    }

    @Test
    public void getConnectString() throws ExtendedSqlException, SQLException
    {
        assertEquals("jdbc:h2:mem:SimpleConnectorTest;DB_CLOSE_DELAY=-1;USER=sa;PASSWORD=sa", connector.getConnectString());
    }
}
