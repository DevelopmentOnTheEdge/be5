package com.developmentontheedge.be5.databasemodel.groovy;

import com.developmentontheedge.be5.api.services.GroovyRegister;
import com.developmentontheedge.beans.DynamicProperty;
import com.developmentontheedge.beans.DynamicPropertySet;
import com.developmentontheedge.beans.DynamicPropertySetDecorator;
import com.developmentontheedge.beans.DynamicPropertySetSupport;

import java.util.Iterator;

/**
 * Created by ruslan on 11.12.15.
 */
public class FlexibleDynamicPropertySet extends DynamicPropertySetDecorator
{
    static {
        GroovyRegister.registerMetaClass( FlexibleDynamicPropertySetMetaClass.class, FlexibleDynamicPropertySet.class );
    }

    public FlexibleDynamicPropertySet( DynamicPropertySet dps )
    {
        super( dps );
    }

    public DynamicPropertySet getDelegate()
    {
        return delegateDps;
    }

    public static DynamicPropertySet getInstance()
    {
        return getInstance( new DynamicPropertySetSupport() );
    }

    public static DynamicPropertySet getInstance( DynamicPropertySet dps )
    {
        FlexibleDynamicPropertySet ret = new FlexibleDynamicPropertySet( dps );
//        DefaultGroovyMethods.setMetaClass( ret, FlexibleDynamicPropertySetMetaClass.class );
        return ret;
    }

    public static DynamicPropertySet unwrap( DynamicPropertySet dps )
    {
        Iterator<DynamicProperty> i = dps.propertyIterator();
        while( i.hasNext() ) {
            DynamicProperty dp = i.next();
            if( dp.getValue() instanceof DynamicPropertySet )
            {
                if( dp.getValue() instanceof FlexibleDynamicPropertySet )
                {
                    dp.setValue( ( ( FlexibleDynamicPropertySet )dp.getValue() ).getDelegate() );
                }
                unwrap( ( DynamicPropertySet )dp.getValue() );
            }
        }
        return dps instanceof FlexibleDynamicPropertySet ? ( ( FlexibleDynamicPropertySet )dps ).getDelegate() : dps;
    }
}
