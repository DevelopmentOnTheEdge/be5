package com.developmentontheedge.be5.test.mocks;

import com.developmentontheedge.be5.exceptions.Be5Exception;
import com.developmentontheedge.be5.database.DbService;
import com.developmentontheedge.be5.database.sql.ResultSetParser;
import com.developmentontheedge.be5.database.sql.SqlExecutor;
import com.developmentontheedge.be5.database.sql.SqlExecutorVoid;
import org.apache.commons.dbutils.ResultSetHandler;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import static org.mockito.Mockito.mock;


public class DbServiceMock implements DbService
{
    private static final Logger log = Logger.getLogger(DbServiceMock.class.getName());

    public static DbService mock = mock(DbService.class);

    public static void clearMock()
    {
        mock = mock(DbService.class);
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
    public int updateWithoutBeSql(String sql, Object... params)
    {
        log.warning("Unsafe update (not be-sql parsed and formatted): " + sql + Arrays.toString(params));
        return mock.update(sql, params);
    }

    @Override
    public <T> T insert(String sql, Object... params)
    {
        log.fine(sql + Arrays.toString(params));
        return mock.insert(sql, params);
    }

    @Override
    public <T> T execute(SqlExecutor<T> executor)
    {
        return mock.execute(executor);
    }

    @Override
    public <T> T transactionWithResult(SqlExecutor<T> executor)
    {
        try {
            return executor.run(null);
        } catch (SQLException e) {
            throw Be5Exception.internal(e);
        }
    }

    @Override
    public void transaction(SqlExecutorVoid executor)
    {
        try {
            executor.run(null);
        } catch (SQLException e) {
            throw Be5Exception.internal(e);
        }
    }
}