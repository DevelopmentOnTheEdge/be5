package com.developmentontheedge.be5.operation.databasemodel.impl;

@SuppressWarnings( "serial" )
public class InvalidClassTypeException extends RuntimeException {

    public InvalidClassTypeException( String reason, Class clazz ) 
    {
        super( reason + " " + clazz.getCanonicalName() );
    }  

}
