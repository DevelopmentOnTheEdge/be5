package com.developmentontheedge.be5.databasemodel.impl;

import com.developmentontheedge.be5.api.exceptions.Be5Exception;
import com.developmentontheedge.be5.databasemodel.MethodProvider;
import com.developmentontheedge.be5.databasemodel.RecordModel;
import com.developmentontheedge.beans.DynamicPropertySet;
import com.developmentontheedge.beans.DynamicPropertySetBlocked;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;


public class RecordModelBase extends DynamicPropertySetBlocked implements RecordModel
{
    private final EntityModelBase entityModelBase;
    private final String id;

    RecordModelBase(EntityModelBase entityModelBase, DynamicPropertySet dps)
    {
        super( dps );
        if(dps.getProperty(entityModelBase.getPrimaryKeyName()) == null)
        {
            throw Be5Exception.internal("DynamicPropertySet not contain primaryKey '" + entityModelBase.getPrimaryKeyName() + "'");
        }
        id = dps.getProperty(entityModelBase.getPrimaryKeyName()).getValue().toString();
        this.entityModelBase = entityModelBase;
    }

    @Override
    public String getId()
    {
        return id;
    }

    @Override
    public int remove()
    {
        return entityModelBase.remove( getId() );
    }

    @Override
    public String toString()
    {
        return super.toString() + " { " + this.getClass().getSimpleName() + " [ " + entityModelBase.getPrimaryKeyName() + " = " + getId() + " ] }";
    }

    @Override
    public Object invokeMethod( String methodName, Object... arguments )
    {
        Method method = ExtendedModels.getInstance().getMethod( entityModelBase, methodName );
        return new MethodProviderBase( method ).invoke( arguments );
    }

    public MethodProvider getMethod(String methodName )
    {
        Method method = ExtendedModels.getInstance().getMethod( entityModelBase, methodName );
        return new MethodProviderBase( method );
    }

    @Override
    public void update( String propertyName, Object value )
    {
        entityModelBase.set( getId(), propertyName, value );
        super.setValueHidden( propertyName, value );
    }

    @Override
    public void update( Map<String, Object> values )
    {
        entityModelBase.set( getId(), values );
        for( String propertyName : values.keySet() )
        {
            if( super.hasProperty( propertyName ) )
            {
                super.setValueHidden( propertyName, values.get( propertyName ) );
            }
        }
    }

    @Override
    public void setValue( String propertyName, Object value )
    {
        throw new IllegalAccessError( "You can't use this operation. Use EntityModel#set() to update value in database." );
    }

    public class MethodProviderBase implements MethodProvider
    {
        protected final Method method;

        MethodProviderBase( Method method )
        {
            this.method = method;
        }

        @Override
        public Object invoke()
        {
            return invoke( new Object[]{} );
        }

        @Override
        public Object invoke( Object... args )
        {
            try
            {
                Object[] fullArgs = new Object[ args.length + 1 ];
                fullArgs[ 0 ] = RecordModelBase.this;
                System.arraycopy(args, 0, fullArgs, 1, args.length);
                return method.invoke( entityModelBase, fullArgs );
            }
            catch( IllegalAccessException | IllegalArgumentException | InvocationTargetException e )
            {
                throw new RuntimeException( e );
            }
        }
    }
}
