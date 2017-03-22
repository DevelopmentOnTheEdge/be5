package com.developmentontheedge.be5.api.services.impl;

import com.developmentontheedge.be5.api.services.DatabaseService;
import com.developmentontheedge.be5.api.services.SqlService;
import com.developmentontheedge.sql.format.Context;
import com.developmentontheedge.sql.format.Dbms;
import com.developmentontheedge.sql.format.Formatter;
import com.developmentontheedge.sql.model.DefaultParserContext;
import com.developmentontheedge.sql.model.SqlQuery;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SqlServiceImpl implements SqlService
{
    private static final Logger log = Logger.getLogger(SqlServiceImpl.class.getName());

    private QueryRunner queryRunner;

    public SqlServiceImpl(DatabaseService databaseService){
        queryRunner = new QueryRunner(databaseService.getDataSource());
    }

    @Override
    public <T> T select(String sql, ResultSetHandler<T> rsh, Object... params)
    {
        sql = format(sql);
        try
        {
            return queryRunner.query(sql, rsh, params);
        }
        catch (SQLException e)
        {
            throw propagate(e);
        }
    }

    @Override
    public <T> List<T> selectAll(String sql, ResultSetHandler<T> rsh, Object... params)
    {
        sql = format(sql);
        try
        {
            return queryRunner.query(sql, rs -> {
                List<T> res = new ArrayList<>();

                while (rs.next()){
                    res.add(rsh.handle(rs));
                }

                return res;
            }, params);
        }
        catch (SQLException e)
        {
            throw propagate(e);
        }
    }

    @Override
    public int update(String sql, Object... params) {
        try
        {
            return queryRunner.update(sql, params);
        }
        catch (SQLException e)
        {
            throw propagate(e);
        }
    }

    @Override
    public <T> T insert(String sql, ResultSetHandler<T> rsh, Object... params) {
        try
        {
            return queryRunner.insert(sql, rsh, params);
        }
        catch (SQLException e)
        {
            throw propagate(e);
        }
    }

    private String format(String sql)
    {
        //TODO get Dbms from DatabaseServiceImpl
        return new Formatter().format(SqlQuery.parse(sql), new Context(Dbms.MYSQL), new DefaultParserContext());
    }

    private RuntimeException propagate(Exception e) {
        log.log(Level.SEVERE, e.getMessage(), e);
        if (e instanceof RuntimeException) {
            return (RuntimeException) e;
        }

        return new RuntimeException(e);
    }
}
