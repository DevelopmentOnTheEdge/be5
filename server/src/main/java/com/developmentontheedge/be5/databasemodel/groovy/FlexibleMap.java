package com.developmentontheedge.be5.databasemodel.groovy;

import com.developmentontheedge.be5.api.services.GroovyRegister;
import groovy.lang.MetaClassImpl;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ruslan on 14.12.15.
 */
public class FlexibleMap<K,V> extends HashMap<K,V>
{
    public static class FlexibleMapMetaClass<K,V> extends MetaClassImpl
    {
        public FlexibleMapMetaClass( Class theClass )
        {
            super( theClass );
        }

        @Override
        public Object getProperty( Object self, String name )
        {
            Map<Object,Object> map = ( Map<Object,Object> )self;
            Object val = map.get( name );
            if( val == null )
            {
                return createNode( map, name );
            }
            return val;
        }

        public static Map<?,?> createNode( Map<Object,Object> map, String name )
        {
            Map<?,?> node = new HashMap<>();
            map.put( name, node );
            return node;
        }

    }

    static {
        GroovyRegister.registerMetaClass( FlexibleMapMetaClass.class, FlexibleMap.class );
    }
}
