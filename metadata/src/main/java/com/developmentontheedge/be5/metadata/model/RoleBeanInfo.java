package com.developmentontheedge.be5.metadata.model;

import java.beans.PropertyDescriptor;

import com.developmentontheedge.beans.BeanInfoEx;

import com.developmentontheedge.beans.PropertyDescriptorEx;

public class RoleBeanInfo extends BeanInfoEx
{
    public RoleBeanInfo()
    {
        super(Role.class);
    }

    @Override
    protected void initProperties() throws Exception
    {
        add( new PropertyDescriptor( "name", beanClass, "getName", null ) );
        add("usedInExtras");
        add(new PropertyDescriptorEx( "available", beanClass, "isAvailable", null ));
    }
}
