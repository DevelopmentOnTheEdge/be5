package com.developmentontheedge.be5.metadata;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import junit.framework.TestCase;
import oracle.jdbc.driver.OracleConnection;

import com.beanexplorer.enterprise.DatabaseConnector;
import com.beanexplorer.enterprise.client.testframework.TestDB;

public class OraclePerformanceTest extends TestCase
{
    private static String CONNECT_STRING = "jdbc:oracle:thin:@newdev:1521:orcl?user=tisnso_dev;password=tisnso";
    
    public void testLocalizationsNormal() throws SQLException
    {
        DatabaseConnector connector = TestDB.getConnector(CONNECT_STRING);
        ResultSet rs = connector.executeQuery( "SELECT origin, langCode, entity, messagekey, message, topic FROM localizedMessages ORDER BY origin, langCode, entity, messagekey, message" );
        try
        {
            while(rs.next())
            {
                rs.getString( 1 );
                rs.getString( 2 );
                rs.getString( 3 );
                rs.getString( 4 );
                rs.getString( 5 );
                rs.getString( 6 );
            }
        }
        finally
        {
            connector.close( rs );
        }
    }

    public void testLocalizationsFast() throws SQLException
    {
        //for(int prefetch : new int[] {10,20,50,100,200,500,1000,2000,5000})
        for(int prefetch : new int[] {5000,2000,1000,500,200,100,50,20,10})
        {
            long start = System.currentTimeMillis();
            DatabaseConnector connector = TestDB.getConnector(CONNECT_STRING);
            Connection connection = connector.getConnection();
            ((OracleConnection)connection).setDefaultRowPrefetch( prefetch );
            Statement st = null;
            ResultSet rs = null;
            try
            {
                st = connection.createStatement( ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY );
                rs = st.executeQuery( "SELECT origin, langCode, entity, messagekey, message, topic FROM localizedMessages ORDER BY origin, langCode, entity, messagekey, message" );
                while(rs.next())
                {
                    rs.getString( 1 );
                    rs.getString( 2 );
                    rs.getString( 3 );
                    rs.getString( 4 );
                    rs.getString( 5 );
                    rs.getString( 6 );
                }
            }
            finally
            {
                if(rs != null)
                {
                    try
                    {
                        rs.close();
                    }
                    catch ( Exception e )
                    {
                    }
                }
                if(st != null)
                {
                    try
                    {
                        st.close();
                    }
                    catch ( Exception e )
                    {
                    }
                }
                connector.releaseConnection( connection );
            }
            System.out.println( "Prefetch = "+prefetch+"; time = "+(System.currentTimeMillis()-start) );
        }
    }
}
