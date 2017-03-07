package com.developmentontheedge.be5.api.services;

import java.util.List;

import com.developmentontheedge.be5.api.sql.Inserter;
import com.developmentontheedge.be5.api.sql.Selector;
import com.developmentontheedge.be5.api.sql.ExistenceChecker.ExistenceCheckerEndpoint;
import com.developmentontheedge.be5.api.sql.Selector.ResultSetParser;
import com.developmentontheedge.be5.api.sql.Updater.UpdaterSelector;

/**
 * Gives possibility to do simple SQL queries.
 * 
 * @author asko
 */
public interface SqlService
{

    interface TableFacade
    {
        /**
         * Creates an updater.
         */
        UpdaterSelector where(String fieldName, Object fieldValue);
        
        /**
         * Creates an existence checker.
         */
        public ExistenceCheckerEndpoint with(String fieldName, Object fieldValue);
        
        /**
         * Runs an existence check with a simple condition.
         */
        public boolean existsWith(String fieldName, Object fieldValue);
    }
    
    /**
     * Creates a selector.
     */
    Selector from(String tableName);
    
    /**
     * Creates a selector.
     */
    Selector from(String tableName, String... tableNames);
    
    /**
     * Creates an inserter.
     */
    Inserter into(String tableName);
    
    /**
     * Creates an updater or an existence checker.
     */
    TableFacade in(String tableName);
    
    /**
     * Run a query that was formed with <code>SqlBuilder</code> or by calling <code>query.getQueryCompiled().validate()</code>.
     * In the second case the query must not contain any runtime placeholders or nested queries.
     */
    <T> List<T> customSelect(String statement, ResultSetParser<T> parser);
    
}
