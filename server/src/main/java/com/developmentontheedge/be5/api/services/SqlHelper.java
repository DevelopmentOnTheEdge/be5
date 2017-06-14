package com.developmentontheedge.be5.api.services;

import com.developmentontheedge.be5.metadata.DatabaseConstants;
import com.developmentontheedge.be5.metadata.Utils;
import com.developmentontheedge.beans.DynamicPropertySet;

import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;

import static java.util.Collections.singletonList;

public class SqlHelper
{
    private DatabaseService databaseService;
    private SqlService db;
    private DpsExecutor dpsExecutor;
    private Meta meta;

    public SqlHelper(DatabaseService databaseService, SqlService db, DpsExecutor dpsExecutor, Meta meta)
    {
        this.databaseService = databaseService;
        this.db = db;
        this.dpsExecutor = dpsExecutor;
        this.meta = meta;
    }

    public String getConditionsSql(String entity, String primaryKey, Map<?, ?> conditions ) throws SQLException
    {
        StringBuilder sql = new StringBuilder( paramsToCondition( entity, conditions ) );

        if( meta.getColumn( entity, DatabaseConstants.IS_DELETED_COLUMN_NAME ) != null )
        {
            sql.append( " AND " + DatabaseConstants.IS_DELETED_COLUMN_NAME + " != 'yes'" );
        }
        return sql.toString();
    }

    public DynamicPropertySet getRecordByConditions(String entity, String primaryKey, Map<?, ?> conditions ) throws SQLException
    {
        String tableName = entity;

        String sql =
                "SELECT * FROM " + tableName + " WHERE 1 = 1 AND " +
                        getConditionsSql( entity, primaryKey, conditions );

        return db.select(sql, dpsExecutor::getDps);
    }

    public DynamicPropertySet getRecordById( String entity, String primaryKey, Long id ) throws SQLException
    {
        return getRecordById( entity, primaryKey, id, Collections.emptyMap() );
    }

    public DynamicPropertySet getRecordById( String entity, String primaryKey, Long id, Map<String, Object> conditions) throws SQLException
    {
        String tableName = entity;

        String sql = "SELECT * FROM " + tableName
                + " WHERE " + primaryKey + " = " + id;

        if( !conditions.isEmpty() )
        {
            sql += " AND " + paramsToCondition( entity, conditions );
        }

        if( meta.getColumn( entity, DatabaseConstants.IS_DELETED_COLUMN_NAME ) != null )
        {
            sql += " AND " + DatabaseConstants.IS_DELETED_COLUMN_NAME + " != 'yes'";
        }

        return db.select(sql, dpsExecutor::getDps, id);
    }

    public String paramsToCondition( String entity, Map<?,?> values )
    {
        String cond = "";
        for( Map.Entry<?,?> entry : values.entrySet() )
        {
            if( !"".equals( cond ) )
            {
                cond += " AND ";
            }
            String column = entry.getKey().toString();
            Object value = entry.getValue();
            if( value instanceof Object[] )
            {
                cond += "" + column +
                        " IN " + Utils.toInClause(singletonList(value), meta.isNumericColumn( entity, column ) );
                continue;
            }

            String op = " = ";
            if( value instanceof String && ( ( String )value ).endsWith( "%" ) )
            {
                op = " LIKE ";
            }
            cond += "" + column +
                    ( value == null ? " IS NULL " :
                            op + value );
        }

        return cond;
    }
}
