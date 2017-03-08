package com.developmentontheedge.be5.api.sql;

import com.developmentontheedge.be5.util.Generators;
import com.developmentontheedge.be5.util.SqlBuilder;
import com.developmentontheedge.be5.util.SqlBuilder.Change;
import com.developmentontheedge.be5.util.SqlBuilder.Condition;
import com.developmentontheedge.be5.util.SqlStatements;
import com.developmentontheedge.dbms.DbmsConnector;

import java.sql.SQLException;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public class Updater
{
    
    public class UpdaterSelector
    {

        private final UpdaterSelector previous;
        private final Condition field;

        private UpdaterSelector(UpdaterSelector previous, String fieldName, Object fieldValue)
        {
            this.previous = previous;
            this.field = new Condition(fieldName, fieldValue);
        }
        
        private UpdaterSelector(String fieldName, Object fieldValue)
        {
            this(null, fieldName, fieldValue);
        }
        
        /**
         * Adds a condition.
         */
        public UpdaterSelector and(String fieldName, Object fieldValue)
        {
            checkNotNull(fieldName);
            checkNotNull(fieldValue);
            return new UpdaterSelector(fieldName, fieldValue);
        }
        
        /**
         * Defines a change.
         */
        public UpdaterEndpoint set(String fieldName, Object fieldValue)
        {
            checkNotNull(fieldName);
            checkNotNull(fieldValue);
            return new UpdaterEndpoint(this, fieldName, fieldValue);
        }
        
    }
    
    public class UpdaterEndpoint
    {
        
        private final UpdaterSelector selector;
        private final UpdaterEndpoint previous;
        private final Change field;
        
        private UpdaterEndpoint(UpdaterSelector selector, UpdaterEndpoint previous, String fieldName, Object fieldValue)
        {
            this.selector = selector;
            this.previous = previous;
            this.field = new Change(fieldName, fieldValue);
        }
        
        private UpdaterEndpoint(UpdaterSelector selector, String fieldName, Object fieldValue)
        {
            this(selector, null, fieldName, fieldValue);
        }
                
        /**
         * Adds a change.
         */
        public UpdaterEndpoint set(String fieldName, Object fieldValue)
        {
            checkNotNull(fieldName);
            checkNotNull(fieldName);
            return new UpdaterEndpoint(this.selector, this, fieldName, fieldValue);
        }
        
        /**
         * Runs the update.
         * @return number of changes rows
         */
        public int update()
        {
            List<Change> changes = collectChanges();
            List<Condition> conditions = collectConditions();
            String statement = SqlBuilder.create(connector).update(tableName, conditions, changes);
            
            try
            {
                return connector.executeUpdate(statement);
            }
            catch (SQLException e)
            {
                throw new RuntimeSqlException(e);
            }
        }

        private List<Change> collectChanges()
        {
            return Generators.reverseList(this, c -> c.previous, c -> c.field);
        }

        private List<Condition> collectConditions()
        {
            return Generators.reverseList(selector, s -> s.previous, s -> s.field);
        }
        
    }
    
    /**
     * Creates an updater.
     */
    public static Updater in(DbmsConnector connector, String tableName)
    {
        checkNotNull(connector);
        checkNotNull(tableName);
        checkArgument(SqlStatements.isTableName(tableName));
        return new Updater(connector, tableName);
    }
    
    private final String tableName;
    private final DbmsConnector connector;

    private Updater(DbmsConnector connector, String tableName)
    {
        this.connector = connector;
        this.tableName = tableName;
    }
    
    /**
     * Defines a condition.
     */
    public UpdaterSelector where(String fieldName, Object fieldValue)
    {
        checkNotNull(fieldName);
        checkNotNull(fieldValue);
        return new UpdaterSelector(fieldName, fieldValue);
    }

}
