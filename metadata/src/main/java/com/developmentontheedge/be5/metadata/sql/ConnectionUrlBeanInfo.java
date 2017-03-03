package com.developmentontheedge.be5.metadata.sql;

import java.beans.PropertyDescriptor;

import com.developmentontheedge.beans.BeanInfoEx;

public class ConnectionUrlBeanInfo extends BeanInfoEx
{
    public ConnectionUrlBeanInfo()
    {
        super(ConnectionUrl.class);
    }

    @Override
    protected void initProperties() throws Exception
    {
        add("host");
        add("port");
        add("db");
        addHidden(new PropertyDescriptor( "properties", beanClass, "getProperties", null ));
    }
}
