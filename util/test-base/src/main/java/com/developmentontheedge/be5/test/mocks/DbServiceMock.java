package com.developmentontheedge.be5.test.mocks;

import com.developmentontheedge.be5.database.ConnectionService;
import com.developmentontheedge.be5.database.DbService;
import com.developmentontheedge.be5.database.sql.ResultSetParser;
import com.developmentontheedge.be5.database.sql.TransactionExecutor;
import com.developmentontheedge.be5.database.sql.TransactionExecutorVoid;
import com.developmentontheedge.be5.database.SqlExecutor;
import com.developmentontheedge.be5.database.QRec;
import com.developmentontheedge.sql.model.AstStart;
import org.apache.commons.dbutils.ResultSetHandler;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import static org.mockito.Mockito.mock;


public class DbServiceMock implements DbService
{
    private static final Logger log = Logger.getLogger(DbServiceMock.class.getName());

    public static DbService mock = mock(DbService.class);
    private ConnectionService connectionService;

    @Inject
    public DbServiceMock(ConnectionService connectionService)
    {
        this.connectionService = connectionService;
    }

    public static void clearMock()
    {
        mock = mock(DbService.class);
    }

    @Override
    public List<QRec> list( String sql )
    {
        log.fine( sql );
        return mock.list( sql );
    }

    @Override
    public List<QRec> list( String sql, String cacheName )
    {
        log.fine( sql );
        return mock.list( sql, cacheName );
    }

    @Override
    public QRec record( String sql )
    {
        log.fine( sql );
        return mock.record( sql );
    }

    @Override
    public QRec record( String sql, String cacheName )
    {
        log.fine( sql );
        return mock.record( sql, cacheName );
    }

    @Override
    public <T> T query(String sql, ResultSetHandler<T> rsh, Object... params)
    {
        log.fine(sql + Arrays.toString(params));
        return mock.query(sql, rsh, params);
    }

    @Override
    public <T> T select(String sql, ResultSetParser<T> parser, Object... params)
    {
        log.fine(sql + Arrays.toString(params));
        return mock.select(sql, parser, params);
    }

    @Override
    public <T> List<T> list(String sql, ResultSetParser<T> parser, Object... params)
    {
        log.fine(sql + Arrays.toString(params));
        return mock.list(sql, parser, params);
    }

    @Override
    public <T> List<T> list(AstStart astStart, ResultSetParser<T> parser, Object... params)
    {
        log.fine(astStart.getQuery().toString() + Arrays.toString(params));
        return mock.list(astStart.format(), parser, params);
    }

    @Override
    public <T> T one(String sql, Object... params)
    {
        log.fine(sql + Arrays.toString(params));
        return mock.one(sql, params);
    }

    @Override
    public int update(String sql, Object... params)
    {
        log.fine(sql + Arrays.toString(params));
        return mock.update(sql, params);
    }

    @Override
    public int updateRaw(String sql, Object... params)
    {
        log.info("Unsafe update (not be-sql parsed and formatted): " + sql + Arrays.toString(params));
        return mock.updateRaw(sql, params);
    }

    @Override
    public <T> T insert(String sql, Object... params)
    {
        log.fine(sql + Arrays.toString(params));
        return mock.insert(sql, params);
    }

    @Override
    public <T> T insertRaw(String sql, Object... params)
    {
        log.fine(sql + Arrays.toString(params));
        return mock.insertRaw(sql, params);
    }

    @Override
    public <T> T execute(SqlExecutor<T> executor)
    {
        return mock.execute(executor);
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

    @Override
    public String format(String sql)
    {
        return sql;
    }

    @Override
    public String format(AstStart astStart)
    {
        return astStart.format();
    }

    @Override
    public <T> List<T> executeRaw(String sql, ResultSetHandler<T> rsh, Object... params)
    {
        return mock.executeRaw(sql, rsh, params);
    }
}
