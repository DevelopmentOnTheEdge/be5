package com.developmentontheedge.be5.test.mocks;

import com.developmentontheedge.be5.api.services.SqlService;
import com.developmentontheedge.be5.api.sql.ResultSetParser;
import org.apache.commons.dbutils.ResultSetHandler;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import static org.mockito.Mockito.mock;

public class SqlServiceMock implements SqlService
{
    private static final Logger log = Logger.getLogger(SqlServiceMock.class.getName());

    public static SqlService mock = mock(SqlService.class);

    public static void clearMock()
    {
        mock = mock(SqlService.class);
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
    public <T> List<T> selectList(String sql, ResultSetParser<T> parser, Object... params)
    {
        log.fine(sql + Arrays.toString(params));
        return mock.selectList(sql, parser, params);
    }

    @Override
    public <T> T getScalar(String sql, Object... params)
    {
        log.fine(sql + Arrays.toString(params));
        return mock.getScalar(sql, params);
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
}