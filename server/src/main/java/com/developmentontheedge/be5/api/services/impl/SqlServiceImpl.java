package com.developmentontheedge.be5.api.services.impl;

import com.developmentontheedge.be5.api.services.DatabaseService;
import com.developmentontheedge.be5.api.services.SqlService;
import com.developmentontheedge.be5.api.sql.ResultSetParser;
import com.developmentontheedge.be5.api.sql.SqlExecutor;
import com.developmentontheedge.sql.format.Context;
import com.developmentontheedge.sql.format.Formatter;
import com.developmentontheedge.sql.model.DefaultParserContext;
import com.developmentontheedge.sql.model.SqlQuery;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


public class SqlServiceImpl implements SqlService
{
    private static final Logger log = Logger.getLogger(SqlServiceImpl.class.getName());

    private QueryRunner queryRunner;
    private DatabaseService databaseService;

    public SqlServiceImpl(DatabaseService databaseService)
    {
        this.databaseService = databaseService;
        queryRunner = new QueryRunner();
    }

    @Override
    public <T> T query(String sql, ResultSetHandler<T> rsh, Object... params)
    {
        return execute(true, conn -> query(conn, sql, rsh, params));
    }

    @Override
    public <T> T select(String sql, ResultSetParser<T> parser, Object... params)
    {
        return execute(true, conn -> query(conn, sql, rs -> rs.next() ? parser.parse(rs) : null, params));
    }

    @Override
    public <T> List<T> selectList(String sql, ResultSetParser<T> parser, Object... params)
    {
        return execute(true, conn -> query(conn, sql, rs -> {
            List<T> rows = new ArrayList<>();
            while (rs.next()) {
                rows.add(parser.parse(rs));
            }
            return rows;
        }, params));
    }

    @Override
    public <T> T getScalar(String sql, Object... params)
    {
        return execute(true, conn -> query(conn, sql, new ScalarHandler<T>(), params));
    }

    @Override
    public int update(String sql, Object... params)
    {
        return execute(false, conn -> update(conn, sql, params));
    }

    @Override
    public int updateWithoutBeSql(String sql, Object... params)
    {
        return execute(false, conn -> updateUnsafe(conn, sql, params));
    }

    @Override
    public <T> T insert(String sql, Object... params)
    {
        return execute(false, conn -> insert(conn, sql, params));
    }

    private String format(String sql)
    {
        return new Formatter().format(SqlQuery.parse(sql),
                new Context(databaseService.getRdbms().getDbms()), new DefaultParserContext());
    }

    private <T> T query(Connection conn, String sql, ResultSetHandler<T> rsh, Object... params) throws SQLException
    {
        sql = format(sql);
        log.fine(sql + Arrays.toString(params));
        return queryRunner.query(conn, sql , rsh, params);
    }

    private int update(Connection conn, String sql, Object... params) throws SQLException
    {
        sql = format(sql);
        log.fine(sql + Arrays.toString(params));
        return queryRunner.update(conn, sql, params);
    }

    private int updateUnsafe(Connection conn, String sql, Object... params) throws SQLException
    {
        log.warning("Unsafe update (not be-sql parsed and formatted): " + sql + Arrays.toString(params));
        return queryRunner.update(conn, sql, params);
    }

    private <T> T insert(Connection conn, String sql, Object... params) throws SQLException
    {
        sql = format(sql);
        log.fine(sql + Arrays.toString(params));
        return queryRunner.insert(conn, sql, new ScalarHandler<>(), params);
    }

    private <T> T execute(boolean isReadOnly, SqlExecutor<T> executor)
    {
        Connection conn = null;
        Connection txConn = databaseService.getCurrentTxConn();

        try
        {
            conn = (txConn != null) ? txConn : databaseService.getConnection(isReadOnly);
            return executor.run(conn);
        }
        catch (SQLException e)
        {
            log.log(Level.SEVERE, "", e);
            throw new RuntimeException(e);
        }
        finally
        {
            if(txConn == null)
            {
                databaseService.releaseConnection(conn);
            }
        }
    }

}
