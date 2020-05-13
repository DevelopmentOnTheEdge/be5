package com.developmentontheedge.be5.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

public class StringUtils
{

    public static boolean isEmpty(String value)
    {
        return (value == null) || (value).trim().length() == 0;
    }

    public static boolean isNumeric(String value)
    {
        if (isEmpty(value))
        {
            return false;
        }
        //return value.matches( "[-+]?\\d*\\.?\\d+" );
        return value.matches("^[-+]?[0-9]*\\.?[0-9]+([eE][-+]?[0-9]+)?$");
    }

    public static String capitalize(String str)
    {
        return isEmpty(str) ? str : str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }

    public static String join(Object[] array, String delimiter)
    {
        return join(Arrays.asList(array), delimiter);
    }

    public static String join(Object[] array)
    {
        return join(Arrays.asList(array));
    }

    public static String join(Collection c)
    {
        return join(c, "");
    }

    public static String join(Collection c, String delimiter)
    {
        return join(c, delimiter, "");
    }

    public static String join(Collection c, String delimiter, String prefix)
    {
        if (c.isEmpty())
        {
            return "";
        }
        Iterator i = c.iterator();
        StringBuffer result = new StringBuffer().append(prefix).append(i.next());
        while (i.hasNext())
        {
            result.append(delimiter).append(prefix).append(i.next());
        }
        return result.toString();
    }
}
