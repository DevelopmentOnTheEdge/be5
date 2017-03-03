package com.developmentontheedge.be5.metadata.model;

import com.developmentontheedge.beans.PropertyDescriptorEx;

import com.developmentontheedge.beans.BeanInfoEx;

public class ParseResultBeanInfo extends BeanInfoEx
{
    public ParseResultBeanInfo()
    {
        super(ParseResult.class);
    }

    @Override
    protected void initProperties() throws Exception
    {
        add(new PropertyDescriptorEx("result", beanClass, "getResult", null));
        add(new PropertyDescriptorEx("error", beanClass, "getError", null));
    }
}
