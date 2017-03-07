package com.developmentontheedge.be5.api.sql;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import com.developmentontheedge.be5.metadata.sql.DatabaseConnector;
import com.developmentontheedge.be5.util.Generators;
import com.developmentontheedge.be5.util.SqlBuilder;
import com.developmentontheedge.be5.util.SqlStatements;
import com.developmentontheedge.be5.util.SqlBuilder.Condition;

public class ExistenceChecker
{
    
    public class ExistenceCheckerEndpoint
    {
        
        private final ExistenceCheckerEndpoint previous;
        private final Condition condition;
        
        private ExistenceCheckerEndpoint(ExistenceCheckerEndpoint previous, String fieldName, Object fieldValue)
        {
            this.previous = previous;
            this.condition = new Condition(fieldName, fieldValue);
        }

        private ExistenceCheckerEndpoint(String fieldName, Object fieldValue)
        {
            this(null, fieldName, fieldValue);
        }
                
        /**
         * Adds a condition.
         */
        public ExistenceCheckerEndpoint and(String fieldName, Object fieldValue)
        {
            checkNotNull(fieldName);
            checkNotNull(fieldName);
            return new ExistenceCheckerEndpoint(this, fieldName, fieldValue);
        }
        
        /**
         * Runs the query.
         */
        public boolean exists()
        {
            String statement = SqlBuilder.create(connector).selectCountAsValue(tableName, collectConditions());
            
            try
            {
                ResultSet rs = connector.executeQuery(statement);
                
                try
                {
                    if (rs.next())
                    {
                        return rs.getLong("value") == 1;
                    }
                    throw new AssertionError(); // should not happen
                }
                finally
                {
                    connector.close(rs);
                }
            }
            catch (SQLException e)
            {
                throw new RuntimeSqlException(e);
            }
        }
        
        private List<Condition> collectConditions()
        {
            return Generators.reverseList(this, p -> p.previous, p -> p.condition);
        }
        
    }
    
    public static ExistenceChecker in(DatabaseConnector connector, String tableName)
    {
        checkNotNull(connector);
        checkNotNull(tableName);
        checkArgument(SqlStatements.isTableName(tableName));
        return new ExistenceChecker(connector, tableName);
    }
    
    private final DatabaseConnector connector;
    private final String tableName;
    
    private ExistenceChecker(DatabaseConnector connector, String tableName)
    {
        this.connector = connector;
        this.tableName = tableName;
    }
    
    /**
     * Defines a condition.
     */
    public ExistenceCheckerEndpoint with(String fieldName, Object fieldValue)
    {
        checkNotNull(fieldName);
        checkNotNull(fieldName);
        return new ExistenceCheckerEndpoint(fieldName, fieldValue);
    }
    
    /**
     * Runs with a simple condition.
     */
    public boolean existsWith(String fieldName, Object fieldValue)
    {
        checkNotNull(fieldName);
        checkNotNull(fieldName);
        return with(fieldName, fieldValue).exists();
    }
    
}
