package com.developmentontheedge.be5.metadata.model.base;

import java.beans.PropertyDescriptor;

import com.developmentontheedge.beans.BeanInfoEx;

public class BeVectorCollectionBeanInfo extends BeanInfoEx
{
    public BeVectorCollectionBeanInfo()
    {
        super(BeVectorCollection.class);
    }

    @Override
    protected void initProperties() throws Exception
    {
        add(new PropertyDescriptor("name", beanClass, "getName", null));
        add(new PropertyDescriptor("size", beanClass, "getSize", null));
        add(new PropertyDescriptor("errors", beanClass, "hasErrors", null));
    }
}
