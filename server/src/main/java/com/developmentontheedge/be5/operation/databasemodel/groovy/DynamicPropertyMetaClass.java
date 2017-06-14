package com.developmentontheedge.be5.operation.databasemodel.groovy;

import com.developmentontheedge.beans.BeanInfoConstants;
import com.developmentontheedge.beans.DynamicProperty;
import com.developmentontheedge.beans.DynamicPropertySet;

import groovy.lang.DelegatingMetaClass;
import groovy.lang.GroovyObjectSupport;

import java.util.List;

/**
 * Created by ruslan on 26.11.15.
 */
public class DynamicPropertyMetaClass<T extends DynamicPropertySet> extends DelegatingMetaClass
{

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


        private static List<String> getAllPropertyAttributes()
        {
            return null;//JSUtils.listAllStringConstants( BeanInfoConstants.class );
        }

        @Override
        public Object getProperty( String name )
        {
            List<String> attributes = getAllPropertyAttributes();
            if( attributes.contains( name ) )
            {
                try
                {
                    return dp.getAttribute( ( String )BeanInfoConstants.class.getDeclaredField( name ).get( null ) );
                }
                catch( IllegalAccessException | NoSuchFieldException  e )
                {
                    throw new RuntimeException( e );
                }
            }
            else
            {
                return dp.getAttribute( name );
            }
        }

        @Override
        public void setProperty( String name, Object value )
        {
            List<String> attributes = getAllPropertyAttributes( );
            if( attributes.contains( name ) )
            {
                try
                {
                    dp.setAttribute( ( String )BeanInfoConstants.class.getDeclaredField( name ).get( null ), value );
                }
                catch( IllegalAccessException | NoSuchFieldException  e )
                {
                    throw new RuntimeException( e );
                }
            }
            else
            {
                dp.setAttribute( name, value );
            }
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
}
