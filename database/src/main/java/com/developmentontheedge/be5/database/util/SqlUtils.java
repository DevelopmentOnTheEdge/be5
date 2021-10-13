package com.developmentontheedge.be5.database.util;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;

public class SqlUtils
{
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
                return (T) (Integer) ((Long) object).intValue();
            }

            if (Long.class.equals(clazz))
            {
                return (T) longFromDbObject(object);
            }

            if (String.class.equals(clazz))
            {
                return (T) stringFromDbObject(object);
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
