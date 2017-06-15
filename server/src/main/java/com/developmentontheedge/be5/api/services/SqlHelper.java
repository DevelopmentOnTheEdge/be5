package com.developmentontheedge.be5.api.services;

import com.developmentontheedge.be5.annotations.Experimental;
import com.developmentontheedge.be5.api.exceptions.Be5Exception;
import com.developmentontheedge.be5.api.helpers.DpsHelper;
import com.developmentontheedge.be5.metadata.DatabaseConstants;
import com.developmentontheedge.be5.metadata.Utils;
import com.developmentontheedge.be5.operations.InsertOperation;
import com.developmentontheedge.beans.DynamicPropertySet;

import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;

import static java.util.Collections.singletonList;

@Experimental
public class SqlHelper
{
    private DatabaseService databaseService;
    private SqlService db;
    private DpsExecutor dpsExecutor;
    private Meta meta;
    private OperationService operationService;

    public SqlHelper(DatabaseService databaseService, SqlService db, DpsExecutor dpsExecutor, Meta meta, OperationService operationService)
    {
        this.databaseService = databaseService;
        this.db = db;
        this.dpsExecutor = dpsExecutor;
        this.meta = meta;
        this.operationService = operationService;
    }

    public Long insert(String entity, Map<String, String> values)
    {
        String sql = getInsertSQL(entity, values);
        return db.insert(sql, values.values());
//        if( textCallback != null )
//        {
//            textCallback.setText( sql );
//        }
//
//        Pair<Boolean,String> clobResult = updateWithCLOBs( connector, sql, values, entity, pk, true );
//        if( clobResult.getFirst() )
//        {
//            return clobResult.getSecond();
//        }
//
//        //System.err.println( "insert = " + sql );
//        try
//        {
//            if( pk == null || "_dummy_".equals( pk ) || !columnExists( connector, entity, pk ) )
//            {
//                if( pk != null && !"_dummy_".equals( pk ) && !columnExists( connector, entity, pk ) )
//                {
//                    Logger.warn( cat, "Primary column doesn't exists " + entity + "." + pk );
//                }
//                //System.out.println( "executeUpdate = " + entity );
//                connector.executeUpdate( sql );
//                //System.out.println( "!!!!!! executeUpdate = " + entity );
//                return null;
//            }
//
//            if( values.get( pk ) != null && connector.isOracle() && JDBCRecordAdapter.AUTO_IDENTITY.equals( values.get( pk ) ) )
//            {
//                return connector.executeInsert( sql );
//            }
//            else if( values.get( pk ) != null )
//            {
//                connector.executeUpdate( sql );
//                return values.get( pk ).toString();
//            }
//
//            DynamicPropertySet sample = ( DynamicPropertySet )Utils.readTableBean( connector, entity ).clone();
//            OperationSupport.applyMetaData( connector, entity, pk, sample, Collections.EMPTY_MAP, true );
//
//            //java.lang.System.out.println( "pk = " + pk );
//            //java.lang.System.out.println( "sample = " + sample );
//
//            DynamicProperty pkProp = sample.getProperty( pk );
//            if( pkProp != null )
//            {
//                //java.lang.System.out.println( "flag = " + pkProp.getAttribute( JDBCRecordAdapter.AUTO_IDENTITY ) );
//                String defValue = null;
//                if( pkProp.getAttribute( BeanInfoConstants.DEFAULT_VALUE ) != null )
//                {
//                    defValue = pkProp.getAttribute( BeanInfoConstants.DEFAULT_VALUE ).toString();
//                }
//                //System.out.println( "defValue = " + defValue );
//                if( !connector.isOracle() && !Boolean.TRUE.equals( pkProp.getAttribute( JDBCRecordAdapter.AUTO_IDENTITY ) ) ||
//                        connector.isOracle() &&
//                                !( defValue != null &&
//                                        (
//                                                defValue.equalsIgnoreCase( entity ) ||
//                                                        JDBCRecordAdapter.AUTO_IDENTITY.equals( defValue ) ||
//                                                        ( entity + "_" + pk + "_seq" ).equalsIgnoreCase( defValue )
//                                        )
//                                )
//                        )
//                {
//                    //System.err.println( "insert = " + sql );
//                    connector.executeUpdate( sql );
//                    return ( pkProp.getValue() != null ) ? pkProp.getValue().toString() : "";
//                }
//            }
//
//            return connector.executeInsert( sql );
//        }
//        catch( Exception exc )
//        {
//            String trimmedSql = sql.length() < 4000 ? sql : sql.substring( 0, 4000 ) + "...";
//            Logger.warn( cat, "Utils.insert caused exception (" + exc + "), sql = \n" + trimmedSql );
//            throw new Exception( "" + exc.getMessage() + ": Utils.insert:" + trimmedSql, exc );
//        }
    }

    public String getInsertSQL( String entity, Map<String, String> values)
    {
        InsertOperation op = (InsertOperation)operationService.create(new InsertOperation());

        DynamicPropertySet dps = null;
        try
        {
            dps = (DynamicPropertySet)op.getParameters( values );
        }
        catch (Exception e)
        {
            throw Be5Exception.internal(e);
        }

//        for( DynamicProperty prop : dps )
//        {
//            if( Boolean.TRUE.equals( prop.getAttribute( JDBCRecordAdapter.AUTO_IDENTITY ) ) )
//            {
//                String ent = entity + ( tcloneId != null ? tcloneId : "" );
//                sql = "SET IDENTITY_INSERT " + ent + " ON; " + sql + "; SET IDENTITY_INSERT " + ent + " OFF";
//                break;
//            }
//        }

        return op.generateSql( dps );
    }


    public interface InsertSQLTextCallback
    {
        String getText();
        void setText( String text );
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

        String sql = "SELECT * FROM " + tableName + " WHERE 1 = 1 AND "
                      + getConditionsSql( entity, primaryKey, conditions );

        return db.select(sql, DpsHelper::createDps);
    }

    public DynamicPropertySet getRecordById( String entity, Long primaryKey, Long id ) throws SQLException
    {
        return getRecordById( entity, primaryKey, id, Collections.emptyMap() );
    }

    public DynamicPropertySet getRecordById( String entity, Long primaryKey, Long id, Map<String, Object> conditions) throws SQLException
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

        return db.select(sql, DpsHelper::createDps, id);
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
