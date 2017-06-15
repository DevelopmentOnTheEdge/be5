package com.developmentontheedge.be5.operation.databasemodel.groovy;

import java.util.Map;

import com.developmentontheedge.be5.operation.databasemodel.impl.DatabaseModel;

import groovy.lang.MissingMethodException;

public class DatabaseModelMetaClass extends ExtensionMethodsMetaClass
{

    public DatabaseModelMetaClass( Class<DatabaseModel> theClass )
    {
        super( theClass );
    }
    
    @Override
    public Object getProperty( Object object, String property ) 
    {
        if( super.hasProperty( object, property ) != null )
        {
            return super.getProperty( object, property );
        }
        return ( ( DatabaseModel )object ).getEntity( property );
    }
    
    @Override
    public void setProperty( Object object, String entityName, Object values ) 
    {
        ( ( DatabaseModel )object ).getEntity( entityName ).add( ( Map<String, String> )values );
    }

    @Override
    public Object invokeMethod( Object object, String methodName, Object[] arguments ) 
    {
        try
        {
            return super.invokeMethod( object, methodName, arguments );
        }
        catch( MissingMethodException e )
        {
            return ( ( DatabaseModel )object ).getEntity( methodName ).get( ( Map<String, String> )( arguments )[0] );
        }
    }
}
