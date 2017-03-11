package com.developmentontheedge.be5.api.services.impl;

import com.developmentontheedge.be5.api.services.DatabaseService;
import com.developmentontheedge.be5.api.sql.RuntimeSqlException;
import com.developmentontheedge.be5.api.sql.Selector.ResultSetConsumer;
import com.developmentontheedge.be5.api.sql.Selector.ResultSetParser;
import com.developmentontheedge.dbms.DbmsConnector;
import com.google.common.collect.ImmutableList;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * A low-level SQL selection API.
 * 
 * @author asko
 */
public class SelectExecutor
{

    private static class ImmutableListCollector<T> implements ResultSetConsumer
    {
        
        private final ImmutableList.Builder<T> builder;
        private final ResultSetParser<T> parser;
        
        public ImmutableListCollector(ResultSetParser<T> parser)
        {
            this.builder = ImmutableList.builder();
            this.parser = parser;
        }
        
        @Override
        public void accept(ResultSet rs) throws SQLException
        {
            builder.add(parser.parse(rs));
        }
        
        public ImmutableList<T> build()
        {
            return builder.build();
        }
        
    }
    
    private final DatabaseService databaseService;

    public SelectExecutor(DatabaseService databaseService)
    {
        this.databaseService = databaseService;
    }
    
    public void forEach(String statement, ResultSetConsumer consumer)
    {
        DbmsConnector connector = databaseService.getDbmsConnector();
        
        try
        {
            ResultSet rs = connector.executeQuery(statement);
            
            try
            {
                while (rs.next())
                {
                    consumer.accept(rs);
                }
            }
            catch (RuntimeException e)
            {
                throw e;
            }
            finally
            {
                connector.close(rs);
            }
            connector.releaseConnection(connector.getConnection());
        }
        catch (SQLException e)
        {
            throw new RuntimeSqlException(e);
        }
    }
    
    public <T> List<T> select(String statement, ResultSetParser<T> parser)
    {
        ImmutableListCollector<T> collector = new ImmutableListCollector<>(parser);
        forEach(statement, collector);
        
        return collector.build();
    }
    
}
