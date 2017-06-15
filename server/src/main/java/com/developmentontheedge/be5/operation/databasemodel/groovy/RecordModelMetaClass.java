package com.developmentontheedge.be5.operation.databasemodel.groovy;

import java.util.Map;

import com.developmentontheedge.be5.operation.databasemodel.RecordModel;
import com.developmentontheedge.beans.DynamicPropertySet;


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

    public static DynamicPropertySet leftShift( RecordModel rec, Map<String, String> properties )
    {
        rec.update( properties );
        return rec;
    }
}
