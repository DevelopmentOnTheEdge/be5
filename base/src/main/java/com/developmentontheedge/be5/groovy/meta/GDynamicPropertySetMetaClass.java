package com.developmentontheedge.be5.groovy.meta;

//import com.developmentontheedge.beans.DynamicProperty;
import com.developmentontheedge.beans.DynamicPropertySet;
//import groovy.lang.MissingPropertyException;

//public class GDynamicPropertySetMetaClass<T extends DynamicPropertySet> extends ExtensionMethodsMetaClass
public class GDynamicPropertySetMetaClass<T extends DynamicPropertySet> extends DynamicPropertySetMetaClass
{
    public GDynamicPropertySetMetaClass(Class<T> theClass)
    {
        super(theClass);
    }
}
