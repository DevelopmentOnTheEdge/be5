package com.developmentontheedge.be5.api.services.databasemodel.groovy;

import java.util.Map;


import com.developmentontheedge.be5.api.services.databasemodel.impl.DatabaseModel;
import groovy.lang.DelegatingMetaClass;
import groovy.lang.MissingMethodException;

public class DatabaseModelImplMetaClass extends DelegatingMetaClass
{

    public DatabaseModelImplMetaClass(Class<DatabaseModel> theClass )
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
        return ( (DatabaseModel)object ).getEntity( property );
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
            return ( (DatabaseModel)object ).getEntity( methodName ).getBy( ( Map<String, ? super Object> )( arguments )[0] );
        }
    }
}
