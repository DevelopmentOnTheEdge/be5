package com.developmentontheedge.be5.databasemodel.groovy;

import com.developmentontheedge.beans.DynamicProperty;
import com.developmentontheedge.beans.DynamicPropertySet;

import groovy.lang.GroovyObjectSupport;

import java.util.Map;
import java.util.logging.Logger;

/**
 * Created by ruslan on 26.11.15.
 */
public class DynamicPropertyMetaClass<T extends DynamicPropertySet> extends ExtensionMethodsMetaClass
{
    private static final Logger log = Logger.getLogger(DynamicPropertySetMetaClass.class.getName());

    public DynamicPropertyMetaClass( Class<T> theClass )
    {
        super( theClass );
    }

    final public static class AttributeAccessor extends GroovyObjectSupport
    {
        final private DynamicProperty dp;
        private AttributeAccessor( DynamicProperty dp )
        {
            this.dp = dp;
        }


        private static Map<String, String> getAllPropertyAttributes()
        {
            return DynamicPropertySetMetaClass.beanInfoConstants;
        }

        @Override
        public Object getProperty( String name )
        {
            String attributeName = getAllPropertyAttributes().get(name);
            return dp.getAttribute( attributeName != null ? attributeName : name );
        }

        @Override
        public void setProperty( String name, Object value )
        {
            String attributeName = getAllPropertyAttributes().get(name);
            dp.setAttribute( attributeName != null ? attributeName : name, value );
        }
    }


    @Override
    public Object getProperty( Object object, String property )
    {
        DynamicProperty dp = ( DynamicProperty )object;
        if( "attr".equals( property ) )
        {
            return new AttributeAccessor( dp );
        }
        return super.getProperty( object, property );
    }

    public static DynamicProperty leftShift( DynamicProperty dp, Map<String, Object> map )
    {
        removeFromMap( map, "name" );

        Class type = ( Class )removeFromMap( map, "TYPE" );
        Object value = removeFromMap( map, "value" );
        Boolean isHidden = ( Boolean )removeFromMap( map, "HIDDEN" );
        String displayName = asString( removeFromMap( map, "DISPLAY_NAME" ) );

        if( type != null )dp.setType( type );
        if( value != null )dp.setValue( value );
        if( isHidden == Boolean.TRUE )dp.setHidden( true );
        if( displayName != null )dp.setDisplayName( displayName );

        for( String key : map.keySet() )
        {
            String attributeName = DynamicPropertySetMetaClass.beanInfoConstants.get(key);
            if( attributeName != null )
            {
                dp.setAttribute(attributeName, map.get(key));
            }
            else
            {
                log.warning("Not found attribute: " + key + " in BeanInfoConstants");
            }
        }
        return dp;
    }

    private static Object removeFromMap( Map map, Object element )
    {
        if( map.containsKey( element ) )
        {
            return map.remove( element );
        }
        else
        {
            return null;
        }
    }

    private static String asString( Object o )
    {
        return o != null ? o.toString() : null;
    }
}
