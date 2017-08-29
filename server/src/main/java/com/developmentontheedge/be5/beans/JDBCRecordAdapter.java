package com.developmentontheedge.be5.beans;

import com.developmentontheedge.beans.BeanInfoConstants;
import com.developmentontheedge.beans.DynamicProperty;
import com.developmentontheedge.beans.DynamicPropertySetSupport;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.lang.reflect.Method;

/**
 * Implementation of the DynamicPropertySet that is used to present JDBC result sets as dynamic
 * beans recognized by BeanExplorer. This is is an abstract class - in order to use it developer
 * must define getResultSet() function which then will be used to fetch records from the database
 * as well as a database metadata
 */
abstract public class JDBCRecordAdapter extends DynamicPropertySetSupport
{
    public static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String TIME_FORMAT = "HH:mm:ss";
    public static final String DATE_FORMAT = "yyyy-MM-dd";

    public static final String DATABASE_COLUMN_NAME = "database-column-index";
    public static final String DATABASE_COLUMN_LABEL = "database-column-label";
    public static final String DATABASE_TYPE = "database-type";
    public static final String DATABASE_TYPE_NAME = "database-type-name";
    public static final String DATABASE_COLUMN_INDEX = "database-column-index";

    public static final String AUTO_IDENTITY = "auto-identity";

    public static final String NUMERIC_COLUMN = "numeric-column";
    public static final String COMPUTED_COLUMN = "computed-column";
    public static final String DECIMAL_DIGITS = "decimal-digits";

    public static final String DEFAULT_EXPR = "default-expr";

    /**
     * Used to pass a BlobReader object that can read a BLOB on demand
     */
    public static final String BLOB_READER = "blob-reader";

    protected boolean initialized;

    public boolean isInitialized()
    {
        return initialized;
    }

    public JDBCRecordAdapter()
    {
    }

    public JDBCRecordAdapter( boolean bAddAlways )
    {
        this.bAddAlways = bAddAlways;
    }

    @Override
    public Object getValue( String name )
    {
        if( !isInitialized() )
        {
            initialize();
        }
        return super.getValue( name );
    }

    @Override
    public void setValue( String name, Object value )
    {
        if( !isInitialized() )
        {
            initialize();
        }
        super.setValue( name, value );
    }

    /** @return type for the property with specified name. */
    public String getDatabaseType( String name )
    {
        if( !isInitialized() )
        {
            initialize();
        }
        DynamicProperty property = findProperty( name );
        return property != null ? ( String )property.getAttribute( DATABASE_TYPE_NAME ) : null;
    }

    public int getColumnIndex( String name )
    {
        if( !isInitialized() )
        {
            initialize();
        }
        DynamicProperty property = findProperty( name );
        return property != null ? ( ( Integer )property.getAttribute( DATABASE_COLUMN_INDEX ) ).intValue() : -1;
    }

    @Override
    public void add( DynamicProperty property )
    {
        if( !isInitialized() )
        {
            initialize();
        }
        super.add( property );
    }

    @Override
    public Iterator<String> nameIterator()
    {
        if( !isInitialized() )
        {
            initialize();
        }
        return super.nameIterator();
    }

    @Override
    public Iterator<DynamicProperty> propertyIterator()
    {
        if( !isInitialized() )
        {
            initialize();
        }
        return super.propertyIterator();
    }
//
//    public abstract ResultSet getResultSet();
//
//    protected ResultSetMetaData getMetaData(ResultSet rs)
//            throws SQLException
//    {
//        return rs.getMetaData();
//    }
//
//    public int fixType( String colName, int colTypeInt, String colTypeName )
//    {
//        return colTypeInt;
//    }

    protected void initialize()
    {
//        if (initialized)
//            return;
//
//        int maxCol = 0;
//        ResultSet rs;
//        ResultSetMetaData metaData;
//        try
//        {
//            rs = getResultSet();
//            metaData = getMetaData(rs);
//            maxCol = metaData.getColumnCount();
//        }
//        catch( SQLException e )
//        {
//            throw new RuntimeException( e.getMessage() );
//        }
//
//        for( int fieldNo = 1; fieldNo <= maxCol; fieldNo++ )
//        {
//            String forExceptionPropName = null;
//            try
//            {
//                String origColName = metaData.getColumnName( fieldNo );
//                String colName = origColName;
//
//                // tribute to MS SQL Server
//                // which do not return names for expressions like
//                // SELECT 'string' FROM someTable
//                if( colName == null || "".equals( colName ) )
//                    colName = "" + fieldNo;
//
//                String colLabel = metaData.getColumnLabel( fieldNo );
//                if( colLabel != null && !"".equals( colLabel ) && !colLabel.equals( colName ) )
//                {
//                    colName = colLabel;
//                }
//
//                String colType = metaData.getColumnTypeName( fieldNo );
//                int colTypeInt = metaData.getColumnType( fieldNo );
//                colTypeInt = fixType( colName, colTypeInt, colType );
//
//                forExceptionPropName = colName;
//                DynamicProperty prop = makeProperty( rs, colName, getDescriptor(colName), colTypeInt, colType, fieldNo, false, metaData.getScale( fieldNo ) );
//
//                prop.setDisplayName( colLabel );
//
//                // let this to be handled by an aplication rather than at this point
//                //if( metaData.isAutoIncrement( fieldNo ) ) // doesn't work under MySQL though
//                //    pd.setExpert( true );
//
//                prop.setAttribute( DATABASE_COLUMN_NAME, origColName );
//                prop.setAttribute( DATABASE_COLUMN_LABEL, colLabel );
//
//                prop.setAttribute( DATABASE_TYPE, colTypeInt );
//                prop.setAttribute( DATABASE_TYPE_NAME, colType );
//                prop.setAttribute( DATABASE_COLUMN_INDEX, fieldNo );
//
//                if( metaData.isReadOnly( fieldNo ) )
//                    prop.setReadOnly( true );
//
//                if( metaData.isNullable( fieldNo ) != ResultSetMetaData.columnNoNulls )
//                    prop.setAttribute( BeanInfoConstants.CAN_BE_NULL, Boolean.TRUE );
//
//                // finally add the property using low-level method
//                addProperty( prop );
//            }
//            catch( SQLException e )
//            {
//                //e.printStackTrace( System.err );
//                throw new RuntimeException( "columnCount = " + maxCol + ", current = " + fieldNo + ": " + e.getMessage(), e );
//            }
//            catch( IntrospectionException e )
//            {
//                throw new RuntimeException( forExceptionPropName, e );
//            }
//        }
//        initialized = true;
    }

//    public static DynamicProperty makeProperty(
//            ResultSet rs, String colName, int colTypeInt, String colTypeName, int fieldNo, boolean bSetNull, int decimalScale )
//            throws SQLException
//    {
//        return makeProperty(rs, colName, null, colTypeInt, colTypeName, fieldNo, bSetNull, decimalScale);
//    }
//
//    protected PropertyDescriptor getDescriptor(String colName)
//            throws IntrospectionException
//    {
//        return new PropertyDescriptor(colName, null, null);
//    }
//
//    protected static DynamicProperty makeProperty(
//            ResultSet rs, String colName, PropertyDescriptor colDescriptor, int colTypeInt, String colTypeName, int fieldNo, boolean bSetNull, int decimalScale )
//            throws SQLException
//    {
//        // System.out.println( "" + colName + "(" + colTypeName + "): " + colTypeInt );
//
//        // hack for jTDS driver who report 'date' columns as varchars
//        // Later find - doesn't work since colTypeName is reported as "nvarchar"
//        // so left here as reference
//        if( colTypeInt == Types.VARCHAR && "date".equals( colTypeName ) )
//        {
//            colTypeInt = Types.DATE;
//        }
//
//        // SQLite
//        if( colTypeInt == Types.VARCHAR || colTypeInt == Types.CHAR )
//        {
//            if( "DATE".equalsIgnoreCase( colTypeName ) )
//            {
//                colTypeInt = Types.DATE;
//            }
//            else if( "DATETIME".equalsIgnoreCase( colTypeName ) )
//            {
//                colTypeInt = Types.TIMESTAMP;
//            }
//            else if( "TIMESTAMP".equalsIgnoreCase( colTypeName ) )
//            {
//                colTypeInt = Types.TIMESTAMP;
//            }
//            else if( "BLOB".equalsIgnoreCase( colTypeName ) )
//            {
//                colTypeInt = Types.BLOB;
//            }
//            //System.out.println( "HACK    " + colName + "(" + colTypeName + "): " + colTypeInt );
//        }
//
//        switch( colTypeInt )
//        {
//            case Types.CHAR:
//            case Types.VARCHAR:
//            case Types.LONGVARCHAR:
//                return colDescriptor != null ?
//                        new DynamicProperty( colDescriptor, String.class, bSetNull ? null : rs.getString( fieldNo ) ) :
//                        new DynamicProperty( colName, String.class, bSetNull ? null : rs.getString( fieldNo ) ) ;
//
//            case Types.BIT:
//                return colDescriptor != null ?
//                        new DynamicProperty( colDescriptor, Boolean.class, bSetNull ? null : Boolean.valueOf( rs.getBoolean( fieldNo ) ) ) :
//                        new DynamicProperty( colName, Boolean.class, bSetNull ? null : Boolean.valueOf( rs.getBoolean( fieldNo ) ) );
//
//            case Types.BIGINT:
//            {
//                Long val = null;
//                if( !bSetNull )
//                {
//                    boolean wasNull = false;
//                    long longVal = rs.getLong( fieldNo );
//                    try { wasNull = rs.wasNull(); } catch( Throwable ignore ) {}
//                    val = wasNull ? null : Long.valueOf( longVal );
//                }
//                DynamicProperty prop = colDescriptor != null ?
//                        new DynamicProperty( colDescriptor, Long.class, val ) :
//                        new DynamicProperty( colName, Long.class, val );
//                prop.setAttribute( NUMERIC_COLUMN, Boolean.TRUE );
//                return prop;
//            }
//
//            case Types.INTEGER:
//            {
//                Integer val = null;
//                if( !bSetNull )
//                {
//                    boolean wasNull = false;
//                    int intVal = rs.getInt( fieldNo );
//                    try { wasNull = rs.wasNull(); } catch( Throwable ignore ) {}
//                    val = wasNull ? null : Integer.valueOf( intVal );
//                }
//                DynamicProperty prop = colDescriptor != null ?
//                        new DynamicProperty( colDescriptor, Integer.class, val ) :
//                        new DynamicProperty( colName, Integer.class, val );
//                prop.setAttribute( NUMERIC_COLUMN, Boolean.TRUE );
//                return prop;
//            }
//
//            case Types.TINYINT:
//            case Types.SMALLINT:
//            {
//                Short val = null;
//                if( !bSetNull )
//                {
//                    boolean wasNull = false;
//                    short shortVal = 0;
//                    try
//                    {
//                        shortVal = rs.getShort( fieldNo );
//                        try { wasNull = rs.wasNull(); } catch( Throwable ignore ) {}
//                    }
//                    catch( Exception cc )
//                    {
//                        // work around SQL Server's bug which for some reason can't handle getShort
//                        wasNull = true;
//                    }
//                    val = wasNull ? null : Short.valueOf( shortVal );
//
//                }
//                DynamicProperty prop = colDescriptor != null ?
//                        new DynamicProperty( colDescriptor, Short.class, val ) :
//                        new DynamicProperty( colName, Short.class, val );
//                prop.setAttribute( NUMERIC_COLUMN, Boolean.TRUE );
//                return prop;
//            }
//
//            case Types.FLOAT:
//            {
//                Float val = null;
//                if( !bSetNull )
//                {
//                    boolean wasNull = false;
//                    float floatVal = rs.getFloat( fieldNo );
//                    try { wasNull = rs.wasNull(); } catch( Throwable ignore ) {}
//                    val = wasNull ? null : new Float( floatVal );
//                }
//                DynamicProperty prop = colDescriptor != null ?
//                        new DynamicProperty( colDescriptor, Float.class, val ) :
//                        new DynamicProperty( colName, Float.class, val );
//                prop.setAttribute( NUMERIC_COLUMN, Boolean.TRUE );
//                // SQLite
//                if( colTypeName.startsWith( "DECIMAL" ) )
//                {
//                    String typestr = colTypeName.replaceAll( " ", "" );
//                    int ind1 = typestr.indexOf( "," );
//                    if( ind1 > 0 )
//                    {
//                        int ind2 = typestr.indexOf( ")" );
//                        if( ind2 > 0 )
//                        {
//                            prop.setAttribute( DECIMAL_DIGITS, Integer.parseInt( typestr.substring( ind1 + 1, ind2 ) ) );
//                        }
//                    }
//                }
//                return prop;
//            }
//
//            case Types.DOUBLE:
//            case Types.REAL:
//            {
//                Double val = null;
//                if( !bSetNull )
//                {
//                    boolean wasNull = false;
//                    double doubleVal = rs.getDouble( fieldNo );
//                    try { wasNull = rs.wasNull(); } catch( Throwable ignore ) {}
//                    val = wasNull ? null : new Double( doubleVal );
//                }
//                DynamicProperty prop = colDescriptor != null ?
//                        new DynamicProperty( colDescriptor, Double.class, val ) :
//                        new DynamicProperty( colName, Double.class, val );
//                prop.setAttribute( NUMERIC_COLUMN, Boolean.TRUE );
//                return prop;
//            }
//
//            case Types.NUMERIC:
//            case Types.DECIMAL:
//            {
//                BigDecimal val = null;
//                if( !bSetNull )
//                {
//                    boolean wasNull = false;
//                    val = rs.getBigDecimal( fieldNo );
//                    try { wasNull = rs.wasNull(); } catch( Throwable ignore ) {}
//                    val = wasNull ? null : val;
//                    if( val != null && decimalScale > 0 )
//                    {
//                        val = val.setScale( decimalScale, BigDecimal.ROUND_HALF_UP );
//                    }
//                }
//
//                DynamicProperty prop = colDescriptor != null ?
//                        new DynamicProperty( colDescriptor, BigDecimal.class, val ) :
//                        new DynamicProperty( colName, BigDecimal.class, val );
//                prop.setAttribute( NUMERIC_COLUMN, Boolean.TRUE );
//                prop.setAttribute( DECIMAL_DIGITS, decimalScale );
//                return prop;
//            }
//
//            case Types.CLOB:
//            {
//                if( bSetNull )
//                    return colDescriptor != null ?
//                            new DynamicProperty( colDescriptor, String.class, null ) :
//                            new DynamicProperty( colName, String.class, null );
//
//                Clob clob = rs.getClob( fieldNo );
//                boolean wasNull = false;
//                try { wasNull = rs.wasNull(); } catch( Throwable ignore ) {}
//                DynamicProperty prop;
//                if( wasNull )
//                {
//                    prop = colDescriptor != null ?
//                            new DynamicProperty( colDescriptor, String.class, null ) :
//                            new DynamicProperty( colName, String.class, null );
//                }
//                else
//                {
//                    String data = clob.getSubString( 1l, (int)clob.length() );
//                    try
//                    {
//                        Method pmeth = clob.getClass().getMethod( "isTemporary", new Class[ 0 ] );
//                        //System.out.println( colName + ", pmeth " + pmeth );
//                        if( pmeth != null )
//                        {
//                            boolean isTemporary = ( Boolean )pmeth.invoke( clob, new Object[ 0 ] );
//                            if( isTemporary )
//                            {
//                                //System.out.println( "isTemporary = " + colName );
//                                // Already closed
//                                /*
//                                pmeth = clob.getClass().getMethod( "close", new Class[ 0 ] );
//                                if( pmeth != null )
//                                {
//                                    pmeth.invoke( clob, new Object[ 0 ] );
//                                    System.out.println( "close = " + colName );
//                                }
//                                */
//                                pmeth = clob.getClass().getMethod( "freeTemporary", new Class[ 0 ] );
//                                if( pmeth != null )
//                                {
//                                    pmeth.invoke( clob, new Object[ 0 ] );
//                                    //System.out.println( "freed = " + colName + "\n" + data );
//                                    //new Exception().printStackTrace( System.out );
//                                }
//                            }
//                        }
//                        else
//                        {
//                            //System.out.println( "No isTemporary, class " + clob.getClass() );
//                        }
//                    }
//                    catch( NoSuchMethodException ignore )
//                    {
//                    }
//                    catch( Exception exc )
//                    {
//                        //System.out.println( "-------------------------------------------------------" );
//                        //exc.printStackTrace( System.out );
//                    }
//
//                    prop = colDescriptor != null ?
//                            new DynamicProperty( colDescriptor, String.class, data ) :
//                            new DynamicProperty( colName, String.class, data );
//                }
//                return prop;
//            }
//
//            case Types.BLOB:
//            {
//                Object val = null;
//                Class<?> type = Blob.class;
//                if( !bSetNull )
//                {
//                    boolean wasNull = false;
//                    try
//                    {
//                        val = rs.getBlob( fieldNo );
//                    }
//                    catch( SQLException se )
//                    {
//                        if( se.getMessage().indexOf( "not implemented" ) >= 0 )
//                        {
//                            val = rs.getBytes( fieldNo );
//                            type = byte[].class;
//                        }
//                        else
//                        {
//                            throw se;
//                        }
//                    }
//                    try { wasNull = rs.wasNull(); } catch( Throwable ignore ) {}
//                    val = wasNull ? null : val;
//                }
//                if( val instanceof Blob )
//                {
//                    Blob blob = ( Blob )val;
//                    long length = blob.length();
//                    byte []bytes = null;
//                    if( length < 4096 )
//                    {
//                        bytes = blob.getBytes( 1l, ( int )length );
//                    }
//
//                    try
//                    {
//                        Method pmeth = blob.getClass().getMethod( "isTemporary", new Class[ 0 ] );
//                        //System.out.println( colName + ", pmeth " + pmeth );
//                        if( pmeth != null )
//                        {
//                            boolean isTemporary = ( Boolean )pmeth.invoke( blob, new Object[ 0 ] );
//                            if( isTemporary )
//                            {
//                                //System.out.println( "isTemporary = " + colName );
//                                // Already closed
//                                /*
//                                pmeth = clob.getClass().getMethod( "close", new Class[ 0 ] );
//                                if( pmeth != null )
//                                {
//                                    pmeth.invoke( clob, new Object[ 0 ] );
//                                    System.out.println( "close = " + colName );
//                                }
//                                */
//                                pmeth = blob.getClass().getMethod( "freeTemporary", new Class[ 0 ] );
//                                if( pmeth != null )
//                                {
//                                    pmeth.invoke( blob, new Object[ 0 ] );
//                                    System.out.println( "blob freed = " + colName );
//                                    //new Exception().printStackTrace( System.out );
//                                }
//                            }
//                        }
//                        else
//                        {
//                            //System.out.println( "No isTemporary, class " + clob.getClass() );
//                        }
//                    }
//                    catch( NoSuchMethodException ignore )
//                    {
//                    }
//                    catch( Exception exc )
//                    {
//                        //System.out.println( "-------------------------------------------------------" );
//                        //exc.printStackTrace( System.out );
//                    }
//
//
//                    if( length < 4096 )
//                    {
//                        return colDescriptor != null ?
//                                new DynamicProperty( colDescriptor, byte[].class, bytes ) :
//                                new DynamicProperty( colName, byte[].class, bytes );
//                    }
//                }
//                return colDescriptor != null ?
//                        new DynamicProperty( colDescriptor, type, val ) :
//                        new DynamicProperty( colName, type, val );
//            }
//
//            case Types.BINARY:
//            case Types.VARBINARY:
//            case Types.LONGVARBINARY:
//            {
//                Blob val = null;
//                if( !bSetNull )
//                {
//                    boolean wasNull = false;
//                    try
//                    {
//                        try
//                        {
//                            val = rs.getBlob( fieldNo );
//                        }
//                        catch (SQLException e)
//                        {
//                            // some of them (PostgreSQL) doesn't support getBlob but
//                            // provides getBinaryStream and getBytes() instead. Try it.
//                            val = BlobProxy.newInstanse( rs.getBinaryStream( fieldNo ) );
//                        }
//                        wasNull = rs.wasNull();
//                    }
//                    catch ( SQLException e )
//                    {
//                        System.err.println( "Could not obtain Blob object for " + colDescriptor.getName() + ": " + e.getMessage() );
//                    }
//                    val = wasNull ? null : val;
//                }
//                if( val != null )
//                {
//                    long length = val.length();
//                    if( length < 4096 )
//                    {
//                        return colDescriptor != null ?
//                                new DynamicProperty( colDescriptor, byte[].class, val.getBytes( 1l, ( int )length ) ) :
//                                new DynamicProperty( colName, byte[].class, val.getBytes( 1l, ( int )length ) );
//                    }
//                }
//                return colDescriptor != null ?
//                        new DynamicProperty( colDescriptor, Blob.class, val ) :
//                        new DynamicProperty( colName, Blob.class, val );
//            }
//
//            case Types.TIME:
//                return colDescriptor != null ?
//                        new DynamicProperty( colDescriptor, Time.class, bSetNull ? null : rs.getTime( fieldNo ) ) :
//                        new DynamicProperty( colName, Time.class, bSetNull ? null : rs.getTime( fieldNo ) );
//
//            case Types.DATE:
//            {
//                try
//                {
//                    return colDescriptor != null ?
//                            new DynamicProperty( colDescriptor, Date.class, bSetNull ? null : rs.getDate( fieldNo ) ) :
//                            new DynamicProperty( colName, Date.class, bSetNull ? null : rs.getDate( fieldNo ) );
//                }
//                catch( SQLException e )
//                {
//                    // tribute to SQLite
//                    String dateStr = rs.getString( fieldNo );
//                    try
//                    {
//                        if( dateStr.length() == 10 )
//                        {
//                            return colDescriptor != null ?
//                                    new DynamicProperty( colDescriptor, Date.class, new Date( new SimpleDateFormat( DATE_FORMAT ).parse( dateStr ).getTime() ) ) :
//                                    new DynamicProperty( colName, Date.class, new Date( new SimpleDateFormat( DATE_FORMAT ).parse( dateStr ).getTime() ) );
//                        }
//                        else
//                        {
//                            return colDescriptor != null ?
//                                    new DynamicProperty( colDescriptor, Date.class, new Date( new SimpleDateFormat( DATE_TIME_FORMAT ).parse( dateStr ).getTime() ) ) :
//                                    new DynamicProperty( colName, Date.class, new Date( new SimpleDateFormat( DATE_TIME_FORMAT ).parse( dateStr ).getTime() ) );
//                        }
//                    }
//                    catch( java.text.ParseException pe )
//                    {
//                        throw new RuntimeException( pe );
//                    }
//                }
//            }
//
//            case Types.TIMESTAMP:
//            {
//                try
//                {
//                    return colDescriptor != null ?
//                            new DynamicProperty( colDescriptor,
//                                    "DATE".equals( colTypeName ) ? Date.class : Timestamp.class,
//                                    bSetNull ? null : ( "DATE".equals( colTypeName ) ? rs.getDate( fieldNo ) : rs.getTimestamp( fieldNo ) ) ) :
//                            new DynamicProperty( colName,
//                                    "DATE".equals( colTypeName ) ? Date.class : Timestamp.class,
//                                    bSetNull ? null : ( "DATE".equals( colTypeName ) ? rs.getDate( fieldNo ) : rs.getTimestamp( fieldNo ) ) );
//                }
//                catch( SQLException e )
//                {
//                    // tribute to SQLite
//                    String dateStr = rs.getString( fieldNo );
//                    try
//                    {
//                        if( dateStr.length() == 10 )
//                        {
//                            return colDescriptor != null ?
//                                    new DynamicProperty( colDescriptor, Date.class, new Date( new SimpleDateFormat( DATE_FORMAT ).parse( dateStr ).getTime() ) ) :
//                                    new DynamicProperty( colName, Date.class, new Date( new SimpleDateFormat( DATE_FORMAT ).parse( dateStr ).getTime() ) );
//                        }
//                        else
//                        {
//                            return colDescriptor != null ?
//                                    new DynamicProperty( colDescriptor, Timestamp.class, new Timestamp( new SimpleDateFormat( DATE_TIME_FORMAT ).parse( dateStr ).getTime() ) ) :
//                                    new DynamicProperty( colName, Timestamp.class, new Timestamp( new SimpleDateFormat( DATE_TIME_FORMAT ).parse( dateStr ).getTime() ) );
//                        }
//                    }
//                    catch( java.text.ParseException pe )
//                    {
//                        throw new RuntimeException( pe );
//                    }
//                }
//            }
//
//            case Types.JAVA_OBJECT:
//                return colDescriptor != null ?
//                        new DynamicProperty( colDescriptor, Object.class, bSetNull ? null : rs.getObject( fieldNo ) ) :
//                        new DynamicProperty( colName, Object.class, bSetNull ? null : rs.getObject( fieldNo ) ) ;
//
//            default:
//                return colDescriptor != null ?
//                        new DynamicProperty( colDescriptor, String.class, bSetNull ? null : rs.getString( fieldNo ) ) :
//                        new DynamicProperty( colName, String.class, bSetNull ? null : rs.getString( fieldNo ) ) ;
//        }
//    }
}
