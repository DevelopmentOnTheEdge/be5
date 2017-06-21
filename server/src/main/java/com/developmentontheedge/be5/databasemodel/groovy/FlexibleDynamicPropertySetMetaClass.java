package com.developmentontheedge.be5.databasemodel.groovy;

import com.developmentontheedge.beans.DynamicProperty;
import com.developmentontheedge.beans.DynamicPropertySet;
import groovy.lang.MetaClass;
import groovy.lang.MetaClassImpl;
import groovy.lang.MissingPropertyException;
import org.codehaus.groovy.runtime.InvokerHelper;

/**
 * Created by ruslan on 11.12.15.
 */

public class FlexibleDynamicPropertySetMetaClass extends MetaClassImpl
{
    public FlexibleDynamicPropertySetMetaClass( Class theClass )
    {
        super( theClass );
    }

    @Override
    public Object getProperty( Object self, String name )
    {
        DynamicPropertySet dps = ( ( FlexibleDynamicPropertySet )self ).getDelegate();
        MetaClass mc = InvokerHelper.getMetaClass( dps );
        try
        {
            Object node = mc.getProperty( dps, name );
//            System.out.println( node.getClass() );
            if( node instanceof DynamicPropertySet && !( node instanceof FlexibleDynamicPropertySet ) )
            {
                node = FlexibleDynamicPropertySet.getInstance( ( DynamicPropertySet )node );
            }
            return node;
        }
        catch( MissingPropertyException e )
        {
            return createNode( dps, name );
        }
    }

    @Override
    public void setProperty( Object self, String name, Object value )
    {
        DynamicPropertySet dps = ( ( FlexibleDynamicPropertySet )self ).getDelegate();
        MetaClass mc = InvokerHelper.getMetaClass( dps );
        mc.setProperty( dps, name, value );
    }

    private static DynamicPropertySet createNode( DynamicPropertySet dps, String name )
    {
        DynamicPropertySet value = FlexibleDynamicPropertySet.getInstance();
        dps.add( new DynamicProperty( name, DynamicPropertySet.class ) );
        dps.setValue( name, value );
        return value;
    }
}


