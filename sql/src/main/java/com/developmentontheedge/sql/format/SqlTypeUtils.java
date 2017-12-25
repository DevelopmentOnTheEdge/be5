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

    public static boolean isNumber(String className)
    {
        return "java.lang.Long".equals(className) ||
                "java.lang.Integer".equals(className) ||
                "java.lang.Short".equals(className) ||
                "java.lang.Double".equals(className) ||
                "java.lang.Float".equals(className);
    }

    public static Object parseValue(String value, String className)
    {
        if("java.lang.Long".equals(className))
        {
            return Long.valueOf(value);
        }
        else if("java.lang.Integer".equals(className))
        {
            return Integer.valueOf(value);
        }
        if("java.lang.Short".equals(className))
        {
            return Short.valueOf(value);
        }
        else if("java.lang.Double".equals(className))
        {
            return Double.valueOf(value);
        }
        if("java.lang.Float".equals(className))
        {
            return Float.valueOf(value);
        }
        else
        {
            return value;
        }
    }

}
