package com.developmentontheedge.be5.operation.databasemodel.groovy;

public class PropertyAccessHelper {

    private final static String PROPERTY_PREFIX = "_";
    private final static String VALUE_PREFIX = "$";

    public static boolean isPropertyAccess( String v )
    {
        return v != null && v.startsWith( PROPERTY_PREFIX );
    }
    
    public static boolean isValueAccess( String v )
    {
        return v != null && ( v.startsWith( VALUE_PREFIX ) );
    }
}
