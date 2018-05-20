package com.developmentontheedge.be5.api.util;

import java.math.BigDecimal;
import java.math.BigInteger;
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
    public static Object getSqlValue(Class<?> clazz, ResultSet rs, int idx)
    {
        try{
            Object object = rs.getObject(idx);
            if(object == null)
            {
                return null;
            }

            if(clazz == Double.class && object.getClass() == BigDecimal.class)
            {
                return ((BigDecimal)object).doubleValue();
            }

            if(clazz == Long.class && object.getClass() == BigInteger.class)
            {
                return ((BigInteger)object).longValue();
            }

            if(clazz == Short.class && object.getClass() == Integer.class)
            {
                return ((Integer)object).shortValue();
            }

            if(clazz == Integer.class && object.getClass() == Long.class)
            {
                return ((Long)object).intValue();
            }

            if(clazz == String.class && object.getClass() == byte[].class)
            {
                return new String((byte[])object);
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
            catch (SQLException ignore){

            }

            throw new RuntimeException("for column: " + name, e);
        }
    }

    public static Class<?> getTypeClass(int columnType)
    {
        switch( columnType )
        {
            case Types.BIGINT:
                return Long.class;
            case Types.INTEGER:
                return Integer.class;
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
            case Types.CLOB:
                return Clob.class;
            case Types.BLOB:
                return Blob.class;
            case Types.BINARY:
                return byte[].class;
            default:
                return String.class;
        }
    }
}
