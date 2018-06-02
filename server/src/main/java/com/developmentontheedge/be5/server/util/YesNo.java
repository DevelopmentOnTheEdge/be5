package com.developmentontheedge.be5.server.util;

public class YesNo
{
    
    private static final String YES = "yes";
    private static final String NO = "no";
    
    /**
     * Creates a value of YesNo type.
     */
    public static String of(boolean value)
    {
        return value ? YES : NO;
    }
    
    public static String yes()
    {
        return YES;
    }
    
    public static String no()
    {
        return NO;
    }

}
