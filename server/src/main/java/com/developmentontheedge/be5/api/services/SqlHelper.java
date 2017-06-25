package com.developmentontheedge.be5.api.services;

import com.developmentontheedge.be5.api.helpers.DpsHelper;
import com.developmentontheedge.be5.metadata.DatabaseConstants;
import com.developmentontheedge.be5.metadata.Utils;
import com.developmentontheedge.be5.metadata.model.ColumnDef;
import com.developmentontheedge.be5.metadata.model.Entity;
import com.developmentontheedge.beans.BeanInfoConstants;
import com.developmentontheedge.beans.DynamicProperty;
import com.developmentontheedge.beans.DynamicPropertySet;
import com.developmentontheedge.beans.DynamicPropertySetSupport;
import com.developmentontheedge.sql.format.Ast;

import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

import static java.util.Collections.singletonList;

public class SqlHelper
{
    private SqlService db;
    private Meta meta;

    public SqlHelper(SqlService db, Meta meta)
    {
        this.db = db;
        this.meta = meta;
    }

    public DynamicPropertySet getEntityDps(Entity entity)
    {
        Map<String, ColumnDef> columns = meta.getColumns(entity);

        DynamicPropertySet dps = new DynamicPropertySetSupport();

        for (Map.Entry<String, ColumnDef> entry: columns.entrySet())
        {
            ColumnDef columnDef = entry.getValue();
            if(!columnDef.getName().equals(entity.getPrimaryKey()))
            {
                dps.add(getDynamicProperty(columnDef));
            }
        }
        return dps;
    }

    private DynamicProperty getDynamicProperty(ColumnDef columnDef)
    {
        return new DynamicProperty(columnDef.getName(), meta.getColumnType(columnDef));
    }

    public DynamicPropertySet setValuesIfNull(DynamicPropertySet dps, Map<String, String> presetValues)
    {
        StreamSupport.stream(dps.spliterator(), false).forEach(p -> {
            if(p.getValue() == null)p.setValue(presetValues.get(p.getName()));
        });
        return dps;
    }

    public Object[] getValues(DynamicPropertySet dps)
    {
        return StreamSupport.stream(dps.spliterator(), false)
                .map(DynamicProperty::getValue).toArray();
    }

//    public String getConditionsSql(Entity entity, String primaryKey, Map<?, ?> conditions ) throws SQLException
//    {
//        StringBuilder sql = new StringBuilder( paramsToCondition( entity, conditions ) );
//
//        if( meta.getColumn( entity, DatabaseConstants.IS_DELETED_COLUMN_NAME ) != null )
//        {
//            sql.append( " AND " + DatabaseConstants.IS_DELETED_COLUMN_NAME + " != 'yes'" );
//        }
//        return sql.toString();
//    }
//
//    public DynamicPropertySet getRecordByConditions(Entity entity, String primaryKey, Map<?, ?> conditions ) throws SQLException
//    {
//        String tableName = entity.getName();
//
//        String sql = "SELECT * FROM " + tableName + " WHERE 1 = 1 AND "
//                      + getConditionsSql( entity, primaryKey, conditions );
//
//        return db.select(sql, DpsHelper::createDps);
//    }
//
    public DynamicPropertySet getRecordById( Entity entity, Long id ) throws SQLException
    {
        return getRecordById( entity, id, Collections.emptyMap() );
    }

    public DynamicPropertySet getRecordById( Entity entity, Long id, Map<String, Object> conditions) throws SQLException
    {
        String sql = "SELECT * FROM " + entity.getName()
                + " WHERE " + entity.getPrimaryKey() + " = ?";

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

    @Deprecated
    public String paramsToCondition( Entity entity, Map<?,?> values )
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

    public String generateInsertSql(Entity entity, DynamicPropertySet dps)
    {
        Object[] columns = StreamSupport.stream(dps.spliterator(), false)
                .map(DynamicProperty::getName)
                .toArray(Object[]::new);

        Object[] valuePlaceholders = StreamSupport.stream(dps.spliterator(), false)
                .map(x -> "?")
                .toArray(Object[]::new);

        return Ast.insert(entity.getName()).fields(columns).values(valuePlaceholders).format();

        // Oracle trick for auto-generated IDs
//            if( connector.isOracle() && colName.equalsIgnoreCase( pk ) )
//            {
//                if( entity.equalsIgnoreCase( value ) || JDBCRecordAdapter.AUTO_IDENTITY.equals( value ) )
//                {
//                    sql.append( "beIDGenerator.NEXTVAL" );
//                }
//                else if( ( entity + "_" + pk + "_seq" ).equalsIgnoreCase( value ) )
//                {
//                    sql.append( value ).append( ".NEXTVAL" );
//                }
//                else
//                {
//                    //in case of not autoincremented PK
//                    justAddValueToQuery( connector, entity, prop, value, sql );
//                }
//            }
//            else if( connector.isOracle() && !connector.isOracle8() &&
//                     "CLOB".equals( prop.getAttribute( JDBCRecordAdapter.DATABASE_TYPE_NAME ) ) )
//            {
//                sql.append( OracleDatabaseAnalyzer.makeClobValue( connector, value ) );
//            }
//            else if( colName.equalsIgnoreCase( WHO_INSERTED_COLUMN_NAME ) )
//            {
//                sql.append( "'" ).append( Utils.safestr( connector, UserInfoHolder.getUserName() ) ).append( "'" );
//            }
//            else if( colName.equalsIgnoreCase( WHO_MODIFIED_COLUMN_NAME ) )
//            {
//                sql.append( "'" ).append( Utils.safestr( connector, userInfo.getUserName() ) ).append( "'" );
//            }
//            if( colName.equalsIgnoreCase( CREATION_DATE_COLUMN_NAME ) )
//            {
//                sql.append( analyzer.getCurrentDateTimeExpr() );
//            }
//            else if( colName.equalsIgnoreCase( MODIFICATION_DATE_COLUMN_NAME ) )
//            {
//                sql.append( analyzer.getCurrentDateTimeExpr() );
//            }
//            else if( colName.equalsIgnoreCase( IS_DELETED_COLUMN_NAME ) )
//            {
//                sql.append( "'no'" );
//            }
//            else if( DBMS_DATE_PLACEHOLDER.equals( value )  )
//            {
//                sql.append( analyzer.getCurrentDateExpr() );
//            }
//            else if( DBMS_DATETIME_PLACEHOLDER.equals( value ) )
//            {
//                sql.append( analyzer.getCurrentDateTimeExpr() );
//            }
//            else if( InsertOperation.FORCE_NULL_PLACEHOLDER.equals( value ) )
//            {
//                sql.append( "NULL" );
//            }
//
//            else if( Boolean.TRUE.equals( prop.getAttribute( PUT_DBMS_DATE_PLACEHOLDER_FLAG ) ) &&
//                     ( value == null || value != null && value.equals( prop.getAttribute( BeanInfoConstants.DEFAULT_VALUE ) ) )
//                   )
//            {
//                sql.append( analyzer.getCurrentDateExpr() );
//            }
//            else if( Boolean.TRUE.equals( prop.getAttribute( PUT_DBMS_DATETIME_PLACEHOLDER_FLAG ) ) &&
//                     ( value == null || value != null && value.equals( prop.getAttribute( BeanInfoConstants.DEFAULT_VALUE ) ) )
//                   )
//            {
//                sql.append( analyzer.getCurrentDateTimeExpr() );
//            }

//            else if( colName.equalsIgnoreCase( IP_INSERTED_COLUMN_NAME ) && userInfo.getRemoteAddr() != null )
//            {
//                sql.append( "'" ).append( Utils.safestr( connector, userInfo.getRemoteAddr() ) ).append( "'" );
//            }
//            else if( colName.equalsIgnoreCase( IP_MODIFIED_COLUMN_NAME ) && userInfo.getRemoteAddr() != null )
//            {
//                sql.append( "'" ).append( Utils.safestr( connector, userInfo.getRemoteAddr() ) ).append( "'" );
//            }
        //else
//            {
//                justAddValueToQuery( databaseService, "entity", prop, value, sql );
//            }


    }

    public String generateDeleteInSql(Entity entity, int count) {
        return "DELETE FROM "+entity.getName()+" WHERE " + entity.getPrimaryKey() + " IN " + inClause(count);
    }

    public String inClause(int count){
        return "(" + IntStream.range(0, count).mapToObj(x -> "?").collect(Collectors.joining(", ")) + ")";
    }

    public String generateDeleteSql(Entity entity) {
        return "DELETE FROM "+entity.getName()+" WHERE " + entity.getPrimaryKey() + " = ?";
//        if( Utils.columnExists( connector, table, IS_DELETED_COLUMN_NAME ) )
//        {
//            delSql = "UPDATE " + tName + " SET " + IS_DELETED_COLUMN_NAME + " = 'yes'";
//            if( Utils.columnExists( connector, table, WHO_MODIFIED_COLUMN_NAME ) )
//            {
//                delSql += ", " + WHO_MODIFIED_COLUMN_NAME + " = " + Utils.safestr( connector, userInfo.getUserName(), true );
//            }
//            if( Utils.columnExists( connector, table, MODIFICATION_DATE_COLUMN_NAME ) )
//            {
//                delSql += ", " + MODIFICATION_DATE_COLUMN_NAME + " = " + analyzer.getCurrentDateTimeExpr();
//            }
//        }
//
//        if( dryRun )
//        {
//            delSql = "SELECT " + analyzer.quoteIdentifier( Utils.findPrimaryKeyName( connector, table ) ) + " FROM " + tName;
//        }

    }

//    public static String safeValue( DatabaseService connector, DynamicProperty prop )
//    {
//        Object val = prop.getValue();
////
////        if( val instanceof Wrapper )
////        {
////             val = ( (Wrapper)val ).unwrap();
////        }
////
//        String value;
//        if( val == null )
//        {
//            value = "";
//        }
//        else if( val instanceof java.sql.Timestamp )
//        {
//            value = new SimpleDateFormat( connector.getRdbms() == Rdbms.ORACLE ? "yyyy-MM-dd HH:mm:ss" : "yyyy-MM-dd HH:mm:ss.SSS" ).format( val );
//        }
//        else if( val instanceof java.sql.Time )
//        {
//            value = new SimpleDateFormat( "HH:mm:ss" ).format( val );
//        }
//        else if( val instanceof Date)
//        {
//            java.sql.Date sqlDate = new java.sql.Date( ( ( Date )val ).getTime() );
//            value = sqlDate.toString();
//        }
//        else if( val instanceof Calendar)
//        {
//            value = new SimpleDateFormat( connector.getRdbms() == Rdbms.ORACLE ? "yyyy-MM-dd HH:mm:ss" : "yyyy-MM-dd HH:mm:ss.SSS" ).format( ( ( Calendar )val ).getTime() );
//        }
//        else if( val instanceof Boolean ) // BIT
//        {
//            value = Boolean.TRUE.equals( val ) ? "1" : "0";
//        }
//        // must be encrypted?
////        else if( prop.getName().toLowerCase().startsWith( ENCRYPT_COLUMN_PREFIX ) )
////        {
////            value = CryptoUtils.encrypt( val.toString() );
////            prop.setAttribute( PASSWORD_FIELD, Boolean.TRUE );
////        }
//        else
//        {
//            value = val.toString();
//
//            if( Number.class.isAssignableFrom( prop.getType() ) )
//            {
//                value = value.replace(",", ".");
//            }
//
///*          String orig = value;
//            String sizeStr = ( String )prop.getAttribute( COLUMN_SIZE_ATTR );
//            String encoding = connector.getEncoding();
//            if( value != null && value.length() > 0 && sizeStr != null && encoding != null )
//            {
//                if( connector.isOracle() )
//                {
//                    // truncate value since it causes exception
//                    int size = Integer.parseInt( sizeStr );
//                    byte bytes[] = value.getBytes( encoding );
//                    int length = size;
//                    if( length > bytes.length )
//                        length = bytes.length;
//                    value = new String( bytes, 0, length, encoding );
//
//                    // in case we got into middle of multi-byte char
//                    // From Google
//                    // Unicode 65533 is a substitute character for use when
//                    // a character is found that can't be output in the selected encoding
//                    char last = value.charAt( value.length() - 1 );
//                    if( (int)last == 65533 )
//                    {
//                        value = value.substring( 0, value.length() - 1 );
//                    }
//                }
//            }
//*/
//        }
//
//        if( "".equals( value ) && prop.isCanBeNull() )
//            value = null;
//
//        if( value == null && String.class.equals( prop.getType() ) && !prop.isCanBeNull() )
//            value = "";
//
//        return value;
//    }
}
