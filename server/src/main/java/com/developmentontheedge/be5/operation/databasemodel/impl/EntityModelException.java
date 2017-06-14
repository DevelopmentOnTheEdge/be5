package com.developmentontheedge.be5.operation.databasemodel.impl;

@SuppressWarnings( "serial" )
public class EntityModelException extends RuntimeException {

    final private String entityName;

    EntityModelException( String reason, String entityName, Throwable e )
    {
        super( reason, e );
        this.entityName = entityName;
    }

    EntityModelException( String reason, String entityName )
    {
        super( reason );
        this.entityName = entityName;
    }

    EntityModelException( String entityName, Throwable e )
    {
        super( e );
        this.entityName = entityName;
    }

    EntityModelException( String entityName )
    {
        this.entityName = entityName;
    }
    
    protected String getEntityName()
    {
        return entityName;
    }
    
    protected String buildExceptionDetails() 
    {
        return "[ entityName : " + getEntityName() + "]"; 
    }
}
