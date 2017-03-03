package com.developmentontheedge.be5.metadata.model;

import java.beans.PropertyDescriptor;

import com.developmentontheedge.be5.metadata.model.editors.ClassSelector;
import com.developmentontheedge.be5.metadata.model.editors.DaemonTypeSelector;
import com.developmentontheedge.beans.BeanInfoEx;

public class DaemonBeanInfo extends BeanInfoEx
{
    public DaemonBeanInfo()
    {
        super(Daemon.class);
    }

    @Override
    protected void initProperties() throws Exception
    {
        add(new PropertyDescriptor("name", beanClass, "getName", null));
        add("className", ClassSelector.class);
        add("configSection");
        add("daemonType", DaemonTypeSelector.class);
        add("description");
        add("slaveNo");
    }
}
