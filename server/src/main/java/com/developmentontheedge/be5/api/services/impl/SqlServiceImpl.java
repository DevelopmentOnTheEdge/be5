package com.developmentontheedge.be5.api.services.impl;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Objects.requireNonNull;

import java.util.List;

import com.developmentontheedge.be5.metadata.sql.DatabaseConnector;
import com.developmentontheedge.be5.api.services.DatabaseService;
import com.developmentontheedge.be5.api.services.SqlService;
import com.developmentontheedge.be5.api.sql.ExistenceChecker;
import com.developmentontheedge.be5.api.sql.Inserter;
import com.developmentontheedge.be5.api.sql.Selector;
import com.developmentontheedge.be5.api.sql.Updater;
import com.developmentontheedge.be5.api.sql.ExistenceChecker.ExistenceCheckerEndpoint;
import com.developmentontheedge.be5.api.sql.Selector.ResultSetParser;
import com.developmentontheedge.be5.api.sql.Updater.UpdaterSelector;

public class SqlServiceImpl implements SqlService
{
    
    public class TableFacadeImpl implements TableFacade
    {

        private final String tableName;

        public TableFacadeImpl(String tableName)
        {
            this.tableName = tableName;
        }

        @Override
        public UpdaterSelector where(String fieldName, Object fieldValue)
        {
            return Updater.in(SqlServiceImpl.this.getConnector(), tableName).where(fieldName, fieldValue);
        }

        @Override
        public ExistenceCheckerEndpoint with(String fieldName, Object fieldValue)
        {
            return ExistenceChecker.in(SqlServiceImpl.this.getConnector(), tableName).with(fieldName, fieldValue);
        }
        
        @Override
        public boolean existsWith(String fieldName, Object fieldValue)
        {
            return ExistenceChecker.in(SqlServiceImpl.this.getConnector(), tableName).existsWith(fieldName, fieldValue);
        }
        
    }
    
    private final DatabaseService databaseService;

    public SqlServiceImpl(DatabaseService databaseService)
    {
        this.databaseService = databaseService;
    }
    
    @Override
    public Selector from(String tableName)
    {
        return Selector.from(getConnector(), tableName);
    }
    
    @Override
    public Selector from(String tableName, String... tableNames)
    {
        return Selector.from(getConnector(), tableName, tableNames);
    }
    
    @Override
    public Inserter into(String tableName)
    {
        return Inserter.into(getConnector(), tableName);
    }
    
    @Override
    public TableFacade in(String tableName)
    {
        requireNonNull(tableName);
        return new TableFacadeImpl(tableName);
    }
    
    /**
     * Run a query that was formed with <code>SqlBuilder</code> or by calling <code>query.getQueryCompiled().validate()</code>.
     * In the second case the query must not contain any runtime placeholders or nested queries.
     */
    public <T> List<T> customSelect(String statement, ResultSetParser<T> parser)
    {
        checkNotNull(statement);
        checkNotNull(parser);
        
        return new SelectExecutor(databaseService).select(statement, parser);
    }
    
    private DatabaseConnector getConnector()
    {
        return databaseService.getDatabaseConnector();
    }
    
}
