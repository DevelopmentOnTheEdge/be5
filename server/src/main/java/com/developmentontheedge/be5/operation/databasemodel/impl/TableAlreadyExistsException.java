package com.developmentontheedge.be5.operation.databasemodel.impl;

@SuppressWarnings( "serial" )
public class TableAlreadyExistsException extends Exception 
{
    TableAlreadyExistsException( String tableName, String reason )
    {
        super( "Table name : " + tableName + ", reason: " + reason );
    }
}
