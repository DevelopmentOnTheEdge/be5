package com.developmentontheedge.be5.operation.databasemodel.impl;

@SuppressWarnings( "serial" )
public class ReferenceNotFoundException extends RuntimeException {

    final private String entityFrom;
    final private String entityTo;

    public ReferenceNotFoundException( String entityFrom, String entityTo ) 
    {
        this.entityFrom = entityFrom;
        this.entityTo = entityTo;
    }
    
    public String getEntityFrom()
    {
        return entityFrom;
    }
    
    public String getEntityTo()
    {
        return entityTo;
    }

}
