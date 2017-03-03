package com.developmentontheedge.be5.metadata.model;

import java.beans.PropertyDescriptor;

import com.developmentontheedge.beans.BeanInfoEx;

public class StaticPageBeanInfo extends BeanInfoEx
{
    public StaticPageBeanInfo()
    {
        super(StaticPage.class);
    }

    @Override
    protected void initProperties() throws Exception
    {
        add( new PropertyDescriptor( "name", beanClass, "getName", null ) );
        add("content");
        add("fileName");
    }
}
