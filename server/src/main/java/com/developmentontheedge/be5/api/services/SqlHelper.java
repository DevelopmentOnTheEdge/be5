package com.developmentontheedge.be5.api.services;

import com.developmentontheedge.be5.api.helpers.DpsHelper;
import com.developmentontheedge.be5.metadata.DatabaseConstants;
import com.developmentontheedge.be5.metadata.Utils;
import com.developmentontheedge.be5.metadata.model.ColumnDef;
import com.developmentontheedge.be5.metadata.model.Entity;
import com.developmentontheedge.beans.DynamicProperty;
import com.developmentontheedge.beans.DynamicPropertySet;
import com.developmentontheedge.beans.DynamicPropertySetSupport;

import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;
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

    public DynamicPropertySet getTableDps(Entity entity)
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

    public DynamicPropertySet getTableDps(Entity entity, Map<String, String> values)
    {
        return setValues(getTableDps(entity), values);
    }

    private DynamicProperty getDynamicProperty(ColumnDef columnDef)
    {
        return new DynamicProperty(columnDef.getName(), meta.getColumnType(columnDef));
    }

    private DynamicPropertySet setValues(DynamicPropertySet dps, Map<String, String> presetValues)
    {
        StreamSupport.stream(dps.spliterator(), false).forEach(
                property -> {
                    property.setValue(presetValues.getOrDefault(property.getName(), getDefault(property.getType())));
                }
        );
        return dps;
    }

    protected String getDefault(Class<?> type){
        if(type == Long.class ||type == Integer.class ||type == Double.class ||type == Float.class){
            return "0";
        }
        return "";
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

    public String generateInsertSql(DynamicPropertySet dps, Entity entity)
    {
        String columns = StreamSupport.stream(dps.spliterator(), false)
                .map(DynamicProperty::getName)
                .collect(Collectors.joining(", "));

        String valuePlaceholders = StreamSupport.stream(dps.spliterator(), false)
                .map(x -> "?")
                .collect(Collectors.joining(", "));

        return "INSERT INTO " + entity.getName() +
                " (" + columns + ")" +
                " VALUES" +
                " (" + valuePlaceholders + ")";

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


    public String generateConditionsSql( Entity entity, Map<String,String> values )
    {
        if(values.size()>0){
            String cond = "";
            for( Map.Entry<String,String> entry : values.entrySet() )
            {
                if( !"".equals( cond ) )
                {
                    cond += " AND ";
                }
                String column = entry.getKey();
                String value = entry.getValue();
//                if( value instanceof Object[] )
//                {
//                    cond += "" + column +
//                            " IN " + Utils.toInClause(singletonList(value), meta.isNumericColumn( entity, column ) );
//                    continue;
//                }

                String op = " = ";
                if( value.endsWith( "%" ) )
                {
                    op = " LIKE ";
                }
                cond += "" + column +
                        ( value.equals("null") ? " IS NULL " :
                                op + "?" );
            }

            return cond;
//            return StreamSupport.stream(dps.spliterator(), false)
//                    .map(x -> x + " = ?")
//                    .collect(Collectors.joining(", "));
        }
        return "1 = 1";
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
