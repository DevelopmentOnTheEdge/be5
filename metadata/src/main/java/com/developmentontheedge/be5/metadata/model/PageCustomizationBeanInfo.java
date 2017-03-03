package com.developmentontheedge.be5.metadata.model;

import java.beans.PropertyDescriptor;

import com.developmentontheedge.be5.metadata.model.editors.ModuleSelector;
import com.developmentontheedge.beans.BeanInfoEx;

public class PageCustomizationBeanInfo extends BeanInfoEx
{
    public PageCustomizationBeanInfo()
    {
        super(PageCustomization.class);
    }

    @Override
    protected void initProperties() throws Exception
    {
        add(new PropertyDescriptor("name", beanClass, "getName", null));
        add(new PropertyDescriptor("type", beanClass, "getType", null));
        add(new PropertyDescriptor("domain", beanClass, "getDomain", null));
        add("code");
        add("originModuleName", ModuleSelector.class);
        add("rolesArray");
    }
}
