package com.developmentontheedge.be5.query.model.beans;

import com.developmentontheedge.be5.groovy.meta.DynamicPropertySetMetaClass;
import com.developmentontheedge.be5.groovy.meta.GroovyRegister;
import com.developmentontheedge.beans.DynamicPropertySet;
import com.developmentontheedge.beans.DynamicPropertySetSupport;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.SQLException;


public class QRec extends DynamicPropertySetSupport
{
    static
    {
        GroovyRegister.registerMetaClass(DynamicPropertySetMetaClass.class, QRec.class);
    }

    public QRec()
    {
    }

    public QRec(DynamicPropertySet dps)
    {
        super(dps);
    }

    /**
     * Retrieves first value. Useful when we need only one column
     *
     * @return value of column
     */
    public Object getValue()
    {
        return properties.get(0).getValue();
    }

    /**
     * Retrieves first value as a string.
     *
     * @return value of column
     */
    public String getString()
    {
        return valToString(getValue());
    }

    private static String valToString(Object val)
    {
        try
        {
            if (val == null)
                return null;
            if (val instanceof Blob)
                return new String(((Blob) val).getBytes(1, (int) ((Blob) val).length()), StandardCharsets.UTF_8);
            if (val instanceof byte[])
                return new String((byte[]) val, StandardCharsets.UTF_8);
        }
        catch (SQLException e)
        {
            return null;
        }
        return val.toString();
    }

    /**
     * Retrieves first value as an int.
     *
     * @return integer value of column or <code>null</code> if it's <code>null</code>.
     */
    public Integer getInt()
    {
        return (null == getValue()) ? null : Integer.valueOf(getValue().toString());
    }

    /**
     * Retrieves first value as a long.
     *
     * @return long value of column or <code>null</code> if it's <code>null</code>.
     */
    public Long getLong()
    {
        return (null == getValue()) ? null : Long.valueOf(getValue().toString());
    }

    /**
     * Retrieves value of the specified column name as string.
     *
     * @param name column
     * @return value of column
     */
    public String getString(String name)
    {
        return valToString(getValue(name));
    }

    /**
     * Retrieves value of the specified column name as int.
     *
     * @param name column
     * @return value of column
     */
    public int getInt(String name)
    {
        return Integer.parseInt(getValue(name).toString());
    }

    /**
     * Retrieves value of the specified column name as long.
     *
     * @param name column
     * @return value of column
     */
    public long getLong(String name)
    {
        return Long.parseLong(getValue(name).toString());
    }

    public java.sql.Date getDate(String name)
    {
        java.util.Date date = (java.util.Date) getValue(name);
        if (date == null)
        {
            return null;
        }
        return new java.sql.Date(date.getTime());
    }

    public java.sql.Date getDate()
    {
        java.util.Date date = (java.util.Date) getValue();
        if (date == null)
        {
            return null;
        }
        return new java.sql.Date(date.getTime());
    }

    public InputStream getBinaryStream() throws SQLException
    {
        Object val = getValue();
        return getBinaryStream(val, properties.get(0).getName());
    }

    public InputStream getBinaryStream(String name) throws SQLException
    {
        Object val = getValue(name);
        return getBinaryStream(val, name);
    }

    private InputStream getBinaryStream(Object val, String name) throws SQLException
    {
        if (val == null)
        {
            return null;
        }
        else if (val instanceof byte[])
        {
            return new ByteArrayInputStream((byte[]) val);
        }
        else if (val instanceof Clob)
        {
            Clob clob = (Clob) val;
            return clob.getAsciiStream();
        }
        return ((Blob) val).getBinaryStream();
    }
}
