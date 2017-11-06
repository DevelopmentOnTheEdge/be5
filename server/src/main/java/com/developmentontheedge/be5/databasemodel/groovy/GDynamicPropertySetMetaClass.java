package com.developmentontheedge.be5.databasemodel.groovy;

import com.developmentontheedge.be5.api.exceptions.Be5Exception;
import com.developmentontheedge.beans.DynamicPropertySet;


public class GDynamicPropertySetMetaClass<T extends DynamicPropertySet> extends ExtensionMethodsMetaClass
{
    public GDynamicPropertySetMetaClass(Class<T> theClass )
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
        throw Be5Exception.internal("use dps[\"nameColumn\"]");
    }

}
