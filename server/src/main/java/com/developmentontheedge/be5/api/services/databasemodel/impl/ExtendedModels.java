package com.developmentontheedge.be5.api.services.databasemodel.impl;

import com.developmentontheedge.be5.exceptions.Be5Exception;
import com.developmentontheedge.be5.api.services.SqlService;
import com.developmentontheedge.be5.api.services.databasemodel.EntityMethod;
import com.developmentontheedge.be5.api.services.databasemodel.EntityModel;
import com.developmentontheedge.beans.DynamicPropertySet;

import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;



final public class ExtendedModels {
    
    volatile private Map<String, Map<String, Method>> entitiesWithMethods = new HashMap<String, Map<String, Method>>();
    private ExtendedModels() {
        
    }

    public static ExtendedModels getInstance()
    {
        return ExtendedModelsAccess.getInstance();
    }
    
    private static class ExtendedModelsAccess
    {
        private static ExtendedModels instance = new ExtendedModels();
        
        private static ExtendedModels getInstance()
        {
            return instance;
        }
    }
    
    public Method getMethod(EntityModel owner, String methodName )
    {
        return getMethods( owner ).get( methodName );
    }
    
    Map<String, Method> getMethods( EntityModel owner )
    {
        Map<String, Method> methods = entitiesWithMethods.get( owner.getEntityName() );
        
        if( methods == null )
        {
            synchronized( owner.getClass() ) 
            {
                methods = entitiesWithMethods.get( owner.getEntityName() );
                if( methods == null )
                {
                    entitiesWithMethods.put( owner.getEntityName(), methods = getMethods( owner.getClass() ) );
                }
                return methods;
            }
        }
        else
        {
            return methods;
        }
    }

    private Map<String, Method> getMethods( Class clazz ) 
    {
        Map<String, Method> methods = new HashMap<String, Method>();
        
        for( Method method : clazz.getMethods() )
        {
            if( method.getAnnotation( EntityMethod.class ) != null )
            {
                Class<?> paramTypes[] = method.getParameterTypes();
                if( paramTypes.length == 0 )
                {
                    throw Be5Exception.internal( "Loaded method have no parameters. " + clazz.getCanonicalName() );
                }
                if( !DynamicPropertySet.class.isAssignableFrom( paramTypes[0] ) )
                {
                    throw Be5Exception.internal( "First parameter must be a string to provide record. " + clazz.getCanonicalName() );
                }

                methods.put( method.getName(), method );
            }
        }
        return Collections.unmodifiableMap( methods );
    }
    
    public static boolean isAvailable( SqlService sqlService ) throws SQLException
    {
        return false;
        //return Utils.columnExists( connector, "entities", "entityModel" );
    }
}
