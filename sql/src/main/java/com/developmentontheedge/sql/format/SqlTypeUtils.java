package com.developmentontheedge.sql.format;


public class SqlTypeUtils
{
    public static boolean isNumber(Class<?> type)
    {
        return type == Long.class ||
                type == Integer.class ||
                type == Short.class ||
                type == Double.class ||
                type == Float.class;
    }

    public static Object parseValue(String value, String className)
    {
        if("java.lang.Double".equals(className))
        {
            return Double.valueOf(value);
        }
        else if("java.lang.Long".equals(className))
        {
            return Long.valueOf(value);
        }
        else
        {
            return value;
        }
    }

}
