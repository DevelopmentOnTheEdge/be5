package com.developmentontheedge.be5.metadata.util;

public class StringUtils {

    public static boolean isEmpty( String value )
    {
        return ( value == null ) || ( value ).trim().length() == 0  ;
    }
}
