package com.developmentontheedge.be5.metadata.model;

import java.beans.PropertyDescriptor;

import com.developmentontheedge.beans.BeanInfoEx;

public class EntityLocalizationsBeanInfo extends BeanInfoEx
{
    public EntityLocalizationsBeanInfo()
    {
        super(EntityLocalizations.class);
    }

    @Override
    protected void initProperties() throws Exception
    {
        add(new PropertyDescriptor("pairs", beanClass, "getPairs", null));
    }
}
