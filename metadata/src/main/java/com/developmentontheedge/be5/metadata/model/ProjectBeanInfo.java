package com.developmentontheedge.be5.metadata.model;

import java.beans.PropertyDescriptor;

import com.developmentontheedge.be5.metadata.model.editors.ConnectionProfileNameSelector;
import com.developmentontheedge.be5.metadata.model.editors.FeaturesSelector;
import com.developmentontheedge.beans.BeanInfoEx;

public class ProjectBeanInfo extends BeanInfoEx
{
    public ProjectBeanInfo()
    {
        super(Project.class);
    }
    
    @Override
    public void initProperties() throws Exception
    {
        super.initProperties();
        add( new PropertyDescriptor( "name", beanClass, "getName", null ) );
        add("connectionProfileName", ConnectionProfileNameSelector.class);
        add("featuresArray", FeaturesSelector.class);
        add("projectFileStructure");
        add("connectedBugtrackersArray");
    }   
}
