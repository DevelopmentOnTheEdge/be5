package com.developmentontheedge.be5.database.test;

import com.developmentontheedge.be5.database.DataSourceService;
import com.developmentontheedge.sql.format.dbms.Dbms;
import org.h2.jdbcx.JdbcDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;


public class DataSourceServiceTestImpl implements DataSourceService
{
    private final DataSource dataSource;

    public DataSourceServiceTestImpl()
    {
        JdbcDataSource ds = new JdbcDataSource();
        ds.setURL("jdbc:h2:~/be5_database_tests");
        ds.setUser("sa");
        ds.setPassword("sa");

        try
        {
            Connection connection = ds.getConnection();
            Statement statement = connection.createStatement();
            statement.execute("DROP TABLE IF EXISTS persons; " +
                    "CREATE TABLE persons ( id BIGSERIAL PRIMARY KEY, name VARCHAR(255) NOT NULL, " +
                    "password VARCHAR(255) NOT NULL, email VARCHAR(255), age INT);");

            statement.close();
            connection.close();
        } catch (SQLException e)
        {
            e.printStackTrace();
        }

        dataSource = ds;
    }

    @Override
    public Dbms getDbms()
    {
        return Dbms.H2;
    }

    @Override
    public DataSource getDataSource()
    {
        return dataSource;
    }

}
