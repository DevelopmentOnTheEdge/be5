package com.developmentontheedge.be5.databasemodel.groovy;

import com.developmentontheedge.be5.databasemodel.RecordModel;
import com.developmentontheedge.beans.DynamicPropertySet;

import java.util.Map;

public class RecordModelMetaClass extends DynamicPropertySetMetaClass
{

    public RecordModelMetaClass( Class<? extends RecordModel> theClass )
    {
        super( theClass );
    }

    @Override
    public void setProperty( Object object, String propertyName, Object value )
    {
        RecordModel rec = ( ( RecordModel )object );

        if( rec.getProperty( propertyName ) != null )
        {
            rec.update( propertyName, value.toString() );
        }
        else
        {
            throw new IllegalArgumentException( "Property " + propertyName + " not found!" );
        }
    }

}
