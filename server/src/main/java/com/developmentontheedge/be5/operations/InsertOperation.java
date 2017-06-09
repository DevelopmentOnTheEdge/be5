package com.developmentontheedge.be5.operations;

import com.developmentontheedge.be5.operation.Operation;
import com.developmentontheedge.be5.operation.OperationContext;
import com.developmentontheedge.be5.operation.OperationSupport;
import com.developmentontheedge.beans.DynamicProperty;
import com.developmentontheedge.beans.DynamicPropertySet;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;


public class InsertOperation extends OperationSupport implements Operation
{

    @Override
    public Object getParameters(Map<String, String> presetValues) throws Exception
    {
        dps = getTableBean(getInfo().getEntity().getName());
        setValues(dps, presetValues);

        return dps;
    }

    @Override
    public void invoke(Object parameters, OperationContext context) throws Exception {
        db.insert(getSQL(dps), getValues(dps));
    }

    private String getSQL(DynamicPropertySet parameters) throws Exception
    {
        StringBuilder sql = new StringBuilder();

        String columns = StreamSupport.stream(parameters.spliterator(), false)
                .map(DynamicProperty::getName)
                .collect(Collectors.joining(", "));

        String values = StreamSupport.stream(parameters.spliterator(), false).map(x -> escapeQuotes(x.getType()) ? "'?'" : "?")
                .collect(Collectors.joining(", "));

        sql.append("INSERT INTO ")
                .append(getInfo().getEntity().getName())
                .append(" (").append(columns).append(")")
                .append(" VALUES")
                .append(" (").append(values).append(")");

        return sql.toString();

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

//    public String safeValue( Object value, Class<?> type ){
//
//    }

    public boolean escapeQuotes(Class<?> type){
        if(type.isAssignableFrom(Integer.class) || type.isAssignableFrom(Long.class)
            || type.isAssignableFrom(Double.class) || type.isAssignableFrom(Float.class)
            || type.isAssignableFrom(Boolean.class)){
            return false;
        }

        return true;
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
