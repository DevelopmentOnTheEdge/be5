package com.developmentontheedge.be5.database.impl;

import com.developmentontheedge.be5.lifecycle.Start;
import com.developmentontheedge.be5.cache.Be5Caches;
import com.developmentontheedge.be5.database.ConnectionService;
import com.developmentontheedge.be5.database.DataSourceService;
import com.developmentontheedge.be5.database.DbService;
import com.developmentontheedge.be5.database.RuntimeSqlException;
import com.developmentontheedge.be5.database.SqlExecutor;
import com.developmentontheedge.be5.database.QRec;
import com.developmentontheedge.be5.database.adapters.DpsRecordAdapter;
import com.developmentontheedge.be5.database.adapters.QRecParser;
import com.developmentontheedge.be5.database.sql.ResultSetParser;
import com.developmentontheedge.be5.database.sql.TransactionExecutor;
import com.developmentontheedge.be5.database.sql.TransactionExecutorVoid;
import com.developmentontheedge.sql.format.MacroExpander;
import com.developmentontheedge.sql.format.dbms.Context;
import com.developmentontheedge.sql.format.dbms.DbmsTransformer;
import com.developmentontheedge.sql.model.AstStart;
import com.developmentontheedge.sql.model.DefaultParserContext;
import com.developmentontheedge.sql.model.SqlQuery;
import com.github.benmanes.caffeine.cache.Cache;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;

import javax.inject.Inject;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

public class DbServiceImpl implements DbService
{
    private static final Logger log = Logger.getLogger(DbServiceImpl.class.getName());

    private Cache<String, String> formatSqlCache;

    private final ConnectionService connectionService;
    private final DataSourceService dataSourceService;
    private final Be5Caches be5Caches;
    private QueryRunner queryRunner;
    private DbmsTransformer dbmsTransformer;

    @Inject
    public DbServiceImpl(ConnectionService connectionService, DataSourceService dataSourceService, Be5Caches be5Caches)
    {
        this.connectionService = connectionService;
        this.dataSourceService = dataSourceService;
        this.be5Caches = be5Caches;
    }

    @Start(order = 20)
    public void start()
    {
        queryRunner = new QueryRunner();
        formatSqlCache = be5Caches.createCache("Format sql");
        Context context = new Context(dataSourceService.getDbms());
        this.dbmsTransformer = context.getDbmsTransformer();
        dbmsTransformer.setParserContext(DefaultParserContext.getInstance());
    }

    @Override
    public List<QRec> list( String sql )
    {
        String finalSql = format( sql );
        return execute( conn -> query( conn, finalSql, rs -> listWrapper(rs, new QRecParser() ) ) );
    }

    @Override
    public List<QRec> list( String sql, String cacheName )
    {
        String finalSql = format( sql );
        return ( List<QRec> )be5Caches.getCache( cacheName ).get( finalSql, k ->
            execute( conn -> query( conn, finalSql, rs -> listWrapper(rs, new QRecParser() ) ) )
        );
    }

    @Override
    public QRec record( String sql )
    {
        String finalSql = format( sql );
        QRec retVal = execute(conn -> query(conn, finalSql, rs ->
            rs.next() ? DpsRecordAdapter.addDp( new QRec(), rs ) : null
        ));
        return retVal.isEmpty() ? null : retVal; 
    }

    @Override
    public QRec record( String sql, String cacheName )
    {
        String finalSql = format( sql );
        QRec retVal = ( QRec )be5Caches.getCache( cacheName ).get( finalSql, k ->
            execute(conn -> query(conn, finalSql, rs ->
                rs.next() ? DpsRecordAdapter.addDp( new QRec(), rs ) : new QRec()
        )));        
        return retVal.isEmpty() ? null : retVal; 
    }

    @Override
    public <T> T query(String sql, ResultSetHandler<T> rsh, Object... params)
    {
        String finalSql = format(sql);
        return execute(conn -> query(conn, finalSql, rsh, params));
    }

    @Override
    public <T> T select(String sql, ResultSetParser<T> parser, Object... params)
    {
        String finalSql = format(sql);
        return execute(conn -> query(conn, finalSql, rs -> rs.next() ? parser.parse(rs) : null, params));
    }

    @Override
    public <T> List<T> list(String sql, ResultSetParser<T> parser, Object... params)
    {
        String finalSql = format(sql);
        return execute(conn -> query(conn, finalSql, rs -> listWrapper(rs, parser), params));
    }

    @Override
    public <T> List<T> list(AstStart astStart, ResultSetParser<T> parser, Object... params)
    {
        String finalSql = format(astStart);
        return execute(conn -> query(conn, finalSql, rs -> listWrapper(rs, parser), params));
    }

    private <T> List<T> listWrapper(ResultSet rs, ResultSetParser<T> parser) throws SQLException
    {
        List<T> rows = new ArrayList<>();
        while (rs.next())
        {
            rows.add(parser.parse(rs));
        }
        return rows;
    }

    @Override
    public <T> T one(String sql, Object... params)
    {
        String finalSql = format(sql);
        return execute(conn -> query(conn, finalSql, new ScalarHandler<>(), params));
    }

    @Override
    public int update(String sql, Object... params)
    {
        String finalSql = format(sql);
        return execute(conn -> update(conn, finalSql, params));
    }

    @Override
    public int updateRaw(String sql, Object... params)
    {
        return execute(conn -> updateRaw(conn, sql, params));
    }

    @Override
    public <T> List<T> executeRaw(String sql, ResultSetHandler<T> rsh, Object... params)
    {
        return execute(conn -> executeRaw(conn, sql, rsh, params));
    }

    @Override
    public <T> T insert(String sql, Object... params)
    {
        String finalSql = format(sql);
        return execute(conn -> insert(conn, finalSql, params));
    }

    @Override
    public <T> T insertRaw(String sql, Object... params)
    {
        return execute(conn -> insert(conn, sql, params));
    }

    @Override
    public String format(String sql)
    {
        return formatSqlCache.get(sql, k -> {
            AstStart astStart = SqlQuery.parse(k);
            new MacroExpander().expandMacros(astStart);
            return format(astStart);
        });
    }

    @Override
    public String format(AstStart astStart)
    {
        dbmsTransformer.transformAst(astStart);
        return astStart.format();
    }

    private <T> T query(Connection conn, String sql, ResultSetHandler<T> rsh, Object... params) throws SQLException
    {
        log.fine(sql + Arrays.toString(params));
        return queryRunner.query(conn, sql, rsh, params);
    }

    private int update(Connection conn, String sql, Object... params) throws SQLException
    {
        log.fine(sql + Arrays.toString(params));
        return queryRunner.update(conn, sql, params);
    }

    private int updateRaw(Connection conn, String sql, Object... params) throws SQLException
    {
        log.fine("Update raw sql: " + sql + Arrays.toString(params));
        return queryRunner.update(conn, sql, params);
    }

    private <T> List<T> executeRaw(Connection conn, String sql, ResultSetHandler<T> rsh, Object... params)
            throws SQLException
    {
        log.fine("Execute raw sql: " + sql + Arrays.toString(params));
        return queryRunner.execute(conn, sql, rsh, params);
    }

    private <T> T insert(Connection conn, String sql, Object... params) throws SQLException
    {
        log.fine(sql + Arrays.toString(params));
        //return queryRunner.insert(conn, sql, new ScalarHandler<>(), params);
        Object id = ( T )queryRunner.insert(conn, sql, new ScalarHandler<>(), params);
        if( id instanceof java.math.BigInteger )
        {
            return ( T )( Long )( ( java.math.BigInteger )id ).longValue();
        }
        return ( T )id;
    }

    @Override
    public <T> T execute(SqlExecutor<T> executor)
    {
        Connection conn = null;
        try
        {
            conn = connectionService.getConnection();
            return executor.run(conn);
        }
        catch (SQLException e)
        {
            throw new RuntimeSqlException("Error executing query", e);
        }
        finally
        {
            connectionService.releaseConnection(conn);
        }
    }

    @Override
    public <T> T inTransaction(TransactionExecutor<T> executor)
    {
        return connectionService.inTransaction(executor);
    }

    @Override
    public void useTransaction(TransactionExecutorVoid executor)
    {
        connectionService.useTransaction(executor);
    }

    @Override
    public boolean isInTransaction()
    {
        return connectionService.isInTransaction();
    }
}
