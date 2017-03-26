package com.developmentontheedge.be5.api.services.impl;

import com.developmentontheedge.be5.api.services.DatabaseService;
import com.developmentontheedge.be5.api.services.SqlService;
import com.developmentontheedge.be5.api.sql.ResultSetParser;
import com.developmentontheedge.sql.format.Context;
import com.developmentontheedge.sql.format.Dbms;
import com.developmentontheedge.sql.format.Formatter;
import com.developmentontheedge.sql.model.DefaultParserContext;
import com.developmentontheedge.sql.model.SqlQuery;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static com.developmentontheedge.be5.api.helpers.JulLoggerUtils.getInternalBe5Exception;

public class SqlServiceImpl implements SqlService
{
    private static final Logger log = Logger.getLogger(SqlServiceImpl.class.getName());

    private ScalarHandler<Long> longHandler = new ScalarHandler<>();
    private ScalarHandler<String> stringHandler = new ScalarHandler<>();

    private QueryRunner queryRunner;

    public SqlServiceImpl(DatabaseService databaseService){
        queryRunner = new QueryRunner(databaseService.getDataSource());
    }

    @Override
    public <T> T query(String sql, ResultSetHandler<T> rsh, Object... params){
        sql = format(sql);
        try
        {
            return queryRunner.query(sql, rsh, params);
        }
        catch (SQLException e)
        {
            throw getInternalBe5Exception(log, e);
        }
    }

    @Override
    public <T> T select(String sql, ResultSetParser<T> parser, Object... params)
    {
        return query(sql, rs -> rs.next() ? parser.parse(rs) : null, params);
    }

    @Override
    public <T> List<T> selectList(String sql, ResultSetParser<T> parser, Object... params)
    {
        return query(sql, rs -> {
            List<T> rows = new ArrayList<>();
            while (rs.next()) {
                rows.add(parser.parse(rs));
            }
            return rows;
        }, params);
    }

    @Override
    public int update(String sql, Object... params) {
        try
        {
            return queryRunner.update(sql, params);
        }
        catch (SQLException e)
        {
            throw getInternalBe5Exception(log, e);
        }
    }

    @Override
    public <T> T insert(String sql, Object... params) {
        try
        {
            return queryRunner.insert(sql, new ScalarHandler<>(), params);
        }
        catch (SQLException e)
        {
            throw getInternalBe5Exception(log, e);
        }
    }

    @Override
    public Long selectLong(String sql, Object... params)
    {
        return query(sql, longHandler, params);
    }

    @Override
    public String selectString(String sql, Object... params)
    {
        return query(sql, stringHandler, params);
    }

    private String format(String sql)
    {
        //TODO get Dbms from DatabaseServiceImpl
        return new Formatter().format(SqlQuery.parse(sql), new Context(Dbms.MYSQL), new DefaultParserContext());
    }

}
