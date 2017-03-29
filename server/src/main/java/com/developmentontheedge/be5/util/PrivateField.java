package com.developmentontheedge.be5.util;

import java.lang.reflect.Field;

import com.developmentontheedge.be5.api.exceptions.Be5Exception;

public class PrivateField
{
    private Field field;

    public PrivateField(Class<?> clazz, String fieldName)
    {
        try
        {
            this.field = clazz.getDeclaredField( fieldName );
            field.setAccessible( true );
        }
        catch( NoSuchFieldException | SecurityException e )
        {
            throw Be5Exception.internal( e );
        }
    }

    public PrivateField(String className, String bundle, String fieldName)
    {
        try
        {
            // TODO
            // Class<?> clazz = bundle == null ? Class.forName( className ) : Platform.getBundle( bundle ).loadClass( className );

            Class<?> clazz = Class.forName( className );
            this.field = clazz.getDeclaredField( fieldName );
            field.setAccessible( true );
        }
        catch( NoSuchFieldException | SecurityException | ClassNotFoundException e )
        {
            throw Be5Exception.internal( e );
        }
    }

    public Object resolve(Object obj)
    {
        try
        {
            return field.get( obj );
        }
        catch( SecurityException | IllegalArgumentException | IllegalAccessException e )
        {
            throw Be5Exception.internal( e );
        }
    }
}
