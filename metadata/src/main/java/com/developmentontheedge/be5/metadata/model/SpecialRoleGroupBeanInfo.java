package com.developmentontheedge.be5.metadata.model;

import java.beans.PropertyDescriptor;

import com.developmentontheedge.beans.BeanInfoEx;

public class SpecialRoleGroupBeanInfo extends BeanInfoEx
{
    public SpecialRoleGroupBeanInfo()
    {
        super(SpecialRoleGroup.class);
    }

    @Override
    protected void initProperties() throws Exception
    {
        add(new PropertyDescriptor( "name", beanClass, "getName", null ));
        add(new PropertyDescriptor( "list", beanClass, "getList", null ));
    }
}
