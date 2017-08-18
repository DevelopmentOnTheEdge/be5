package com.developmentontheedge.be5.databasemodel.groovy;

import com.developmentontheedge.beans.BeanInfoConstants;
import com.developmentontheedge.beans.DynamicProperty;
import com.developmentontheedge.beans.DynamicPropertySet;
import com.developmentontheedge.beans.DynamicPropertySetSupport;

import groovy.lang.MissingPropertyException;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class DynamicPropertySetMetaClass<T extends DynamicPropertySet> extends ExtensionMethodsMetaClass
{
    private static final Logger log = Logger.getLogger(DynamicPropertySetMetaClass.class.getName());

    static final Map<String, String> beanInfoConstants = new HashMap<>();
    static {
        Field[] fields = BeanInfoConstants.class.getDeclaredFields();
        for (Field f : fields)
        {
            if (Modifier.isStatic(f.getModifiers())) {
                try{
                    beanInfoConstants.put(f.getName(),
                            BeanInfoConstants.class.getDeclaredField( f.getName() ).get( null ).toString());
                }
                catch( Exception exc )
                {
                    throw new RuntimeException( exc );
                }
            }
        }
    }

    public DynamicPropertySetMetaClass( Class<T> theClass )
    {
        super( theClass );
    }

    @Override
    @SuppressWarnings( "unchecked" )
    public Object getProperty( Object object, String property )
    {
        if( PropertyAccessHelper.isValueAccess( property ) )
        {
            return ( ( T )object ).getValue( property.substring( 1 ) );
        }
        try
        {
            return super.getProperty( object, property );
        }
        catch( MissingPropertyException e )
        {
            if( PropertyAccessHelper.isPropertyAccess( property ) )
            {
                DynamicProperty prop = ( ( T )object ).getProperty( property.substring( 1 ) );
                if( prop != null )
                {
                    return prop;
                }
            }
            DynamicProperty prop = ( ( T )object ).getProperty( property );
            if( prop != null )
            {
                return prop;
            }
            throw e;
        }
    }

    @Override
    @SuppressWarnings( "unchecked" )
    public void setProperty( Object object, String propertyName, Object value )
    {
        DynamicPropertySet dps = ( ( T )object );
        if( value instanceof Map )
        {
            Map map = ( Map )value;
            map.put( "name", propertyName );
            this.invokeMethod( object, "leftShift", new Object[]{ map } );
            return;
        }
        if( value == null )
        {
            DynamicProperty dp = new DynamicProperty( propertyName, String.class );
            dp.setValue( null );
            dps.add( dp );
            return;
        }

        if( dps.getProperty( propertyName ) != null )
        {
            dps.setValue( propertyName, value );
            return;
        }

        DynamicProperty dp = new DynamicProperty( propertyName, value.getClass() );
        dp.setValue( value );
        dps.add( dp );
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

    public static DynamicPropertySet leftShift( DynamicPropertySet dps, DynamicProperty property )
    {
        dps.add( property );
        return dps;
    }

    public static DynamicPropertySet leftShift( DynamicPropertySet dps, Map<String, Object> properties )
    {
        Map<String, Object> map = new HashMap<>();
        map.putAll( properties );
        String name = asString( removeFromMap( map, "name" ) );
        if( name == null )
        {
            name = "null";
        }
        Object value = removeFromMap( map, "value" );
        String displayName = asString( removeFromMap( map, "DISPLAY_NAME" ) );
        Boolean isHidden = ( Boolean )removeFromMap( map, "HIDDEN" );
        Class type = ( Class )removeFromMap( map, "TYPE" );

//        if( type == java.sql.Date.class && value != null )
//        {
//            value = Utils.changeType( value, java.sql.Date.class );
//        }
        DynamicProperty dp = dps.getProperty( name );
        if( dp == null )
        {
            dp = new DynamicProperty( name, type != null ? type : value != null ? value.getClass() : String.class );
            dps.add( dp );
        }

        dp.setValue( value );
        if( displayName != null )dp.setDisplayName( displayName );
        if( isHidden == Boolean.TRUE )dp.setHidden( true );

        for( String key : map.keySet() )
        {
            String attributeName = beanInfoConstants.get(key);
            if( attributeName != null )
            {
                dp.setAttribute( attributeName, map.get( key ) );
            }
        }
        return dps;
    }

    public static DynamicPropertySet plus( DynamicPropertySet dps2, DynamicPropertySet dps )
    {
        DynamicPropertySet clonedDps = new DynamicPropertySetSupport( dps2 );
        for (DynamicProperty dp : dps)
        {
            try
            {
                clonedDps.add(DynamicPropertySetSupport.cloneProperty(dp));
            } catch (Exception e)
            {
                throw new RuntimeException( e );
            }
        }
        return clonedDps;
    }

    private static String asString( Object o )
    {
        return o != null ? o.toString() : null;
    }
}
