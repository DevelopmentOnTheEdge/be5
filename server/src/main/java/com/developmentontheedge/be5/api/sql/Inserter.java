package com.developmentontheedge.be5.api.sql;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.sql.SQLException;

import com.developmentontheedge.be5.DatabaseConnector;
import com.developmentontheedge.be5.util.SqlBuilder;
import com.developmentontheedge.be5.util.SqlStatements;

public class Inserter
{
    
    public class InserterEndpoint
    {
        private final String[] fieldNames;
        
        private InserterEndpoint(String[] fieldNames)
        {
            this.fieldNames = fieldNames;
        }
        
        /**
         * Inserts a row into the table and returns its identifier.
         * @throws RuntimeSqlException 
         */
        public String insert(Object... values)
        {
            checkState(fieldNames.length == values.length);
            
            String statement = SqlBuilder.create(connector).insert(tableName, fieldNames, values);
            
            try
            {
                return connector.executeInsert(statement);
            }
            catch (SQLException e)
            {
                throw new RuntimeSqlException(e);
            }
        }
        
    }
    
    /**
     * Creates an inserter.
     */
    public static Inserter into(DatabaseConnector connector, String tableName)
    {
        checkNotNull(tableName);
        checkArgument(SqlStatements.isTableName(tableName));
        return new Inserter(connector, tableName);
    }
    
    private final DatabaseConnector connector;
    private final String tableName;
    
    private Inserter(DatabaseConnector connector, String tableName)
    {
        this.connector = connector;
        this.tableName = tableName;
    }
    
    /**
     * Defines the non-nullable fields of the table to insert into.
     */
    public InserterEndpoint withFields(String... fieldNames)
    {
        checkNotNull(fieldNames);
        return new InserterEndpoint(fieldNames);
    }
    
    /**
     * Insert a value into a table that requries no fields.
     */
    public String insert()
    {
        return withFields().insert();
    }
    
}
