package com.developmentontheedge.be5.databasemodel.impl;

import com.developmentontheedge.be5.databasemodel.EntityModel;
import com.developmentontheedge.be5.databasemodel.RecordModel;
import com.developmentontheedge.be5.exceptions.Be5Exception;
import com.developmentontheedge.beans.DynamicPropertySet;
import com.developmentontheedge.beans.DynamicPropertySetBlocked;

import java.util.Map;


public class RecordModelBase<T> extends DynamicPropertySetBlocked implements RecordModel<T>
{
    private final EntityModel<T> entityModel;
    private final T id;

    RecordModelBase(T id, EntityModel<T> entityModel, DynamicPropertySet dps)
    {
        super(dps);
        if (dps.getProperty(entityModel.getPrimaryKeyName()) == null)
        {
            throw Be5Exception.internal("DynamicPropertySet not contain primaryKey '" +
                    entityModel.getPrimaryKeyName() + "'");
        }
        this.id = id;
        this.entityModel = entityModel;
    }

    @Override
    public T getPrimaryKey()
    {
        return id;
    }

    @Override
    public int remove()
    {
        return entityModel.remove(id);
    }

    @Override
    public String toString()
    {
        return super.toString() + " { " + this.getClass().getSimpleName() +
                " [ " + entityModel.getPrimaryKeyName() + " = " + getPrimaryKey() + " ] }";
    }

    @Override
    public int update(String propertyName, Object value)
    {
        int count = entityModel.set(getPrimaryKey(), propertyName, value);
        super.setValueHidden(propertyName, value);
        return count;
    }

    @Override
    public int update(Map<String, ?> values)
    {
        int count = entityModel.set(getPrimaryKey(), values);
        for (Map.Entry<String, ?> property : values.entrySet())
        {
            if (super.hasProperty(property.getKey()))
            {
                super.setValueHidden(property.getKey(), property.getValue());
            }
        }
        return count;
    }

    @Override
    public void setValue(String propertyName, Object value)
    {
        throw new IllegalAccessError("You can't use this operation. " +
                "Use EntityModel#set() to update value in database.");
    }
//
//    public class MethodProviderBase implements MethodProvider
//    {
//        protected final Method method;
//
//        MethodProviderBase( Method method )
//        {
//            this.method = method;
//        }
//
//        @Override
//        public Object invoke()
//        {
//            return invoke( new Object[]{} );
//        }
//
//        @Override
//        public Object invoke( Object... args )
//        {
//            try
//            {
//                Object[] fullArgs = new Object[ args.length + 1 ];
//                fullArgs[ 0 ] = RecordModelBase.this;
//                System.arraycopy(args, 0, fullArgs, 1, args.length);
//                return method.invoke(entityModel, fullArgs );
//            }
//            catch( IllegalAccessException | IllegalArgumentException | InvocationTargetException e )
//            {
//                throw new RuntimeException( e );
//            }
//        }
//    }
}
