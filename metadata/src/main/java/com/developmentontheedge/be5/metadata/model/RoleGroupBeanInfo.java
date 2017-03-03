package com.developmentontheedge.be5.metadata.model;

import java.beans.PropertyDescriptor;

import com.developmentontheedge.beans.BeanInfoEx;

public class RoleGroupBeanInfo extends BeanInfoEx
{
    public RoleGroupBeanInfo()
    {
        super(RoleGroup.class);
    }

    @Override
    protected void initProperties() throws Exception
    {
        add(new PropertyDescriptor( "name", beanClass, "getName", null ));
        add("roleSet");
    }
}
