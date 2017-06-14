package com.developmentontheedge.be5.operation.databasemodel.impl;

import java.sql.SQLException;

@SuppressWarnings( "serial" )
public class EntityModelSQLException extends EntityModelException {

    final private String reasonSql;

    public EntityModelSQLException( String entityName, String reasonSql, SQLException e ) 
    {
        super( entityName, e );
        this.reasonSql = reasonSql;        
    }
    
    @Override
    public String toString()
    {
        return super.toString() + buildExceptionDetails();
    }

    @Override
    protected String buildExceptionDetails() 
    {
        return "[ entityName : " + getEntityName() + ", sql : " + reasonSql + "]"; 
    }

}
