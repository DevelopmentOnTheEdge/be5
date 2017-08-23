package com.developmentontheedge.be5.util;

import com.developmentontheedge.be5.api.exceptions.Be5Exception;

import java.sql.Clob;
import java.sql.SQLException;

public class BlobUtils
{
    public static String getAsString(Object value){
        try
        {
            if(value instanceof Clob)
            {
                Clob clob = (Clob) value;
                return clob.getSubString(1, (int) clob.length());
            }
            return value.toString();
        }
        catch (SQLException e)
        {
            throw Be5Exception.internal(e);
        }
    }
}
