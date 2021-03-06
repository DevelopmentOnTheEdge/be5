package com.developmentontheedge.be5.metadata.model;

import com.developmentontheedge.beans.BeanInfoEx;

import java.beans.PropertyDescriptor;

public class StaticPageBeanInfo extends BeanInfoEx
{
    public StaticPageBeanInfo()
    {
        super(StaticPage.class);
    }

    @Override
    protected void initProperties() throws Exception
    {
        add(new PropertyDescriptor("name", beanClass, "getName", null));
        add("content");
        add("fileName");
    }
}
