package com.developmentontheedge.be5.database.util;

import java.sql.Clob;
import java.sql.SQLException;

public class BlobUtils
{
    public static String getAsString(Object value)
    {
        try
        {
            if (value == null)
            {
                return null;
            }
            else if (value.getClass() == byte[].class)
            {
                return new String((byte[]) value);
            }
            else if (value instanceof Clob)
            {
                Clob clob = (Clob) value;
                return clob.getSubString(1, (int) clob.length());
            }
            return (String) value;
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
    }
}
