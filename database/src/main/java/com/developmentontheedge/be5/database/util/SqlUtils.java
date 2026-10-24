package com.developmentontheedge.be5.database.util;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;

public class SqlUtils
{
    /**
     * True if the given column is an unsigned integer type (MySQL/MariaDB "INT UNSIGNED",
     * "BIGINT UNSIGNED", "SMALLINT UNSIGNED", "TINYINT UNSIGNED", "MEDIUMINT UNSIGNED").
     * <p>
     * Such columns can hold values larger than {@link Integer#MAX_VALUE} (up to 2^32-1 for
     * INT UNSIGNED, 2^64-1 for BIGINT UNSIGNED), so they must be read as {@code long} to avoid
     * signed-integer overflow. Driven by {@code ResultSetMetaData.getColumnTypeName()}; falls
     * back to {@code false} (treat as signed) if the metadata is unavailable, so the method is
     * safe to call on any {@code ResultSet}.
     */
    public static boolean isUnsignedInteger(ResultSetMetaData metaData, int column)
    {
        if (metaData == null)
            return false;
        try
        {
            String typeName = metaData.getColumnTypeName(column);
            return typeName != null && typeName.toUpperCase().contains("UNSIGNED");
        }
        catch (SQLException e)
        {
            return false;
        }
    }

    /**
     * Like {@link #getTypeClass(int)} but takes {@link ResultSetMetaData} into account so that
     * unsigned integer columns (which report as {@code Types.INTEGER} / {@code Types.BIGINT}
     * etc. but can hold values beyond the signed range) are mapped to {@link Long} instead of
     * {@link Integer}.
     */
    public static Class<?> getTypeClass(ResultSetMetaData metaData, int column)
    {
        try
        {
            int columnType = metaData.getColumnType(column);
            String typeName = null;
            try
            {
                typeName = metaData.getColumnTypeName(column);
            }
            catch (SQLException ignore)
            {
                // driver does not expose the type name; fall back to signed mapping
            }
            return typeClassForJdbcType(columnType, typeName);
        }
        catch (SQLException e)
        {
            return getTypeClass(Types.OTHER);
        }
    }

    /**
     * Map a JDBC column type to the Java type to read it as, taking the MySQL/MariaDB
     * {@code UNSIGNED} type-name suffix into account. Pure and driver-agnostic, so it is easy
     * to unit test.
     */
    static Class<?> typeClassForJdbcType(int jdbcType, String columnTypeName)
    {
        boolean unsigned = columnTypeName != null && columnTypeName.toUpperCase().contains("UNSIGNED");
        if (unsigned && (jdbcType == Types.INTEGER || jdbcType == Types.BIGINT
                || jdbcType == Types.TINYINT || jdbcType == Types.SMALLINT))
        {
            return Long.class;
        }
        return getTypeClass(jdbcType);
    }

    public static <T> T getSqlValue(Class<T> clazz, ResultSet rs, int idx)
    {
        try
        {
            Object object = rs.getObject(idx);
            if (object == null)
            {
                return null;
            }

            if (Double.class.equals(clazz) && BigDecimal.class.equals(object.getClass()))
            {
                return (T) (Double) ((BigDecimal) object).doubleValue();
            }
            if (Double.class.equals(clazz) && Float.class.equals(object.getClass()))
            {
                return (T) (Double) ((Float) object).doubleValue();
            }
            if (Short.class.equals(clazz) && Integer.class.equals(object.getClass()))
            {
                return (T) (Short) ((Integer) object).shortValue();
            }

            if (Integer.class.equals(clazz) && Long.class.equals(object.getClass()))
            {
                return (T) Integer.valueOf(narrowToInteger(((Long) object).longValue(), clazz));
            }
            if (Integer.class.equals(clazz) && BigInteger.class.equals(object.getClass()))
            {
                return (T) Integer.valueOf(narrowToInteger(((BigInteger) object).longValue(), clazz));
            }

            if (Long.class.equals(clazz))
            {
                return (T) longFromDbObject(object);
            }

            if (String.class.equals(clazz))
            {
                return (T) stringFromDbObject(object);
            }

            if( java.sql.Timestamp.class.equals(clazz) && java.time.LocalDateTime.class.equals( object.getClass() ) )
            {
                return (T) java.sql.Timestamp.valueOf( ( java.time.LocalDateTime )object );
            }

            if( java.sql.Date.class.equals(clazz) && java.time.LocalDate.class.equals( object.getClass() ) )
            {
                return (T) java.sql.Date.valueOf( ( java.time.LocalDate )object );
            }

            if( byte[].class.equals(clazz) && Blob.class.isAssignableFrom( object.getClass() ) )
            {
                Blob blob = ( Blob )object;
                return ( T )blob.getBytes( 0, ( int )blob.length() );
            }

            return clazz.cast(object);
        }
        catch (Throwable e)
        {
            String name = "";
            try
            {
                name = rs.getMetaData().getColumnName(idx);
            }
            catch (SQLException ignore)
            {

            }

            throw new RuntimeException("for column: " + name, e);
        }
    }

    public static Long longFromDbObject(Object number)
    {
        if (number == null)
        {
            return null;
        }
        else if (Long.class.equals(number.getClass()))
        {
            return (Long) number;
        }
        else if (Integer.class.equals(number.getClass()))
        {
            return ((Integer) number).longValue();
        }
        else if (BigInteger.class.equals(number.getClass()))
        {
            return ((BigInteger) number).longValue();
        }
        else
        {
            return Long.parseLong(number.toString());
        }
    }

    /**
     * Narrow a possibly-large integer value to an {@link int} when it fits, or throw a
     * descriptive {@link RuntimeException} when it does not. Used by {@link #getSqlValue} so that
     * an unsigned column holding a value beyond {@link Integer#MAX_VALUE} fails with a clear
     * message (and only after the column is correctly typed as {@code Long} by
     * {@link #getTypeClass(ResultSetMetaData, int)}); callers that need the full range should
     * use {@code getLong()}.
     */
    static Integer narrowToInteger(long value, Class<?> clazz)
    {
        if (value < Integer.MIN_VALUE || value > Integer.MAX_VALUE)
        {
            throw new RuntimeException("Value " + value + " exceeds the range of "
                    + (clazz == null ? "int" : clazz.getSimpleName())
                    + "; use getLong() for unsigned integer columns");
        }
        return (int) value;
    }

    public static String stringFromDbObject(Object value)
    {
        try
        {
            if (value == null)
            {
                return null;
            }

            Class cls = value.getClass();
 
            if( String.class.equals( cls ) )
            {
                return (String) value;
            }
            else if( byte[].class.equals( cls ) )
            {
                return new String( (byte[])value, StandardCharsets.UTF_8 );
            }
            else if( value instanceof Clob )
            {
                Clob clob = (Clob) value;
                return clob.getSubString(1, (int) clob.length());
            }
            //else if( value instanceof org.postgresql.util.PGobject )
            //{
            //    return ( ( org.postgresql.util.PGobject )pgobject ).getValue();
            //}
            else if( cls.getName().startsWith( "org.postgresql.util.PG" ) ||
                     cls.getName().startsWith( "org.postgresql.geometric.PG" )
                   )
            {
                 java.lang.reflect.Method getValue = cls.getMethod( "getValue", new Class[ 0 ] );
                 return ( String )getValue.invoke( value, new Object[ 0 ] ); 
            }
            return value.toString();
        }
        catch (SQLException|NoSuchMethodException|IllegalAccessException|java.lang.reflect.InvocationTargetException e)
        {
            throw new RuntimeException(e);
        }
    }

    public static Class<?> getTypeClass(int columnType)
    {
        switch (columnType)
        {
            case Types.BIGINT:
                return Long.class;
            case Types.INTEGER:
                return Integer.class;
            case Types.TINYINT:
            case Types.SMALLINT:
                return Short.class;
            case Types.DOUBLE:
            case Types.FLOAT:
            case Types.DECIMAL:
            case Types.REAL:
            case Types.NUMERIC:
                return Double.class;
            case Types.BOOLEAN:
                return Boolean.class;
            case Types.DATE:
                return Date.class;
            case Types.TIME:
                return Time.class;
            case Types.TIMESTAMP:
                return Timestamp.class;
            case Types.BLOB:
                return Blob.class;
            case Types.BINARY:
            case Types.VARBINARY:
                return byte[].class;
            default:
                return String.class;
        }
    }

    public static Class<?> getSimpleStringTypeClass(int columnType)
    {
        switch (columnType)
        {
            case Types.BLOB:
            case Types.BINARY:
            case Types.VARBINARY:
                return String.class;
            default:
                return getTypeClass(columnType);
        }
    }
}
