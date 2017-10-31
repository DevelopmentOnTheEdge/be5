package com.developmentontheedge.be5.model.beans;

import com.developmentontheedge.be5.databasemodel.groovy.DynamicPropertyMetaClass;
import com.developmentontheedge.beans.DynamicProperty;
import com.developmentontheedge.beans.DynamicPropertySet;
import groovy.lang.Closure;
import groovy.lang.DelegatesTo;


public class DynamicPropertyGBuilder
{
    public DynamicProperty add(DynamicPropertySet dynamicPropertySet,
                               @DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = DPSAttributes.class) Closure cl)
    {
        DPSAttributes builder = getBuilder(cl);

        DynamicProperty property = new DynamicProperty(builder.getName(), builder.getTYPE());
        dynamicPropertySet.add(property);
        return DynamicPropertyMetaClass.leftShift(property, builder.getMap());
    }

    public DynamicProperty add(DynamicPropertySet dynamicPropertySet, String propertyName,
                               @DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = DPSAttributes.class) Closure cl)
    {
        DPSAttributes builder = getBuilder(cl);

        DynamicProperty property = new DynamicProperty(propertyName, builder.getTYPE());
        dynamicPropertySet.add(property);
        return DynamicPropertyMetaClass.leftShift(property, builder.getMap());
    }

    public DynamicProperty add(DynamicPropertySet dynamicPropertySet, String propertyName, String displayName,
                               @DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = DPSAttributes.class) Closure cl)
    {
        DPSAttributes builder = getBuilder(cl);

        DynamicProperty property = new DynamicProperty(propertyName, builder.getTYPE());
        dynamicPropertySet.add(property);
        property.setDisplayName(displayName);

        return DynamicPropertyMetaClass.leftShift(property, builder.getMap());
    }

    public DynamicProperty edit(DynamicPropertySet dynamicPropertySet, String propertyName,
                               @DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = DPSAttributes.class) Closure cl)
    {
        DPSAttributes builder = getBuilder(cl);

        return DynamicPropertyMetaClass.leftShift(dynamicPropertySet.getProperty(propertyName), builder.getMap());
    }

    private DPSAttributes getBuilder(Closure cl)
    {
        DPSAttributes builder = new DPSAttributes();
        Closure code = cl.rehydrate(builder, this, this);
        code.setResolveStrategy(Closure.DELEGATE_FIRST);
        code.call();
        return builder;
    }
}
