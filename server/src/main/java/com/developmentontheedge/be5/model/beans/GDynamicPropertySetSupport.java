package com.developmentontheedge.be5.model.beans;

import com.developmentontheedge.be5.databasemodel.groovy.DynamicPropertyMetaClass;
import com.developmentontheedge.be5.databasemodel.groovy.DynamicPropertySetMetaClass;
import com.developmentontheedge.be5.util.Utils;
import com.developmentontheedge.beans.DynamicProperty;
import com.developmentontheedge.beans.DynamicPropertySet;
import com.developmentontheedge.beans.DynamicPropertySetSupport;
import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import org.codehaus.groovy.runtime.GStringImpl;

import java.util.HashMap;
import java.util.Map;


public class GDynamicPropertySetSupport extends DynamicPropertySetSupport
{
    private final Object owner;

    public GDynamicPropertySetSupport(Object owner)
    {
        super();
        this.owner = owner;
    }

    public GDynamicPropertySetSupport(DynamicPropertySet dynamicPropertySet, Object owner)
    {
        super(dynamicPropertySet);
        this.owner = owner;
    }

    public Object call(String name)
    {
        return super.getProperty(name);
    }

    public Object getAt(String name)
    {
        return super.getValue(name);
    }

    public void putAt(String name, Object value)
    {
        super.setValue(name, value);
    }

    public DynamicProperty add(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = DPSAttributes.class) Closure cl)
    {
        DPSAttributes builder = getBuilder(cl);

        DynamicProperty property = new DynamicProperty(builder.getName(), builder.getTYPE());//todo - add get type from value and refactoring TestGroovyOp
        add(property);
        return DynamicPropertyMetaClass.leftShift(property, builder.getMap());
    }

    public DynamicProperty add(String propertyName,
                               @DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = DPSAttributes.class) Closure cl)
    {
        DPSAttributes builder = getBuilder(cl);

        DynamicProperty property = new DynamicProperty(propertyName, builder.getTYPE());//todo - add get type from value and refactoring TestGroovyOp
        add(property);
        return DynamicPropertyMetaClass.leftShift(property, builder.getMap());
    }

    public DynamicProperty add(String propertyName, String displayName,
                               @DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = DPSAttributes.class) Closure cl)
    {
        DPSAttributes builder = getBuilder(cl);

        DynamicProperty property = new DynamicProperty(propertyName, builder.getTYPE());
        add(property);
        property.setDisplayName(displayName);

        return DynamicPropertyMetaClass.leftShift(property, builder.getMap());
    }

    public DynamicProperty edit(String propertyName,
                                @DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = DPSAttributes.class) Closure cl)
    {
        DPSAttributes builder = getBuilder(cl);

        return DynamicPropertyMetaClass.leftShift(getProperty(propertyName), builder.getMap());
    }

    private DPSAttributes getBuilder(Closure cl)
    {
        DPSAttributes builder = new DPSAttributes();
        Closure code = cl.rehydrate(builder, owner, owner);
        code.setResolveStrategy(Closure.DELEGATE_FIRST);
        code.call();
        return builder;
    }

    @Deprecated
    public DynamicPropertySet leftShift( Map<String, Object> properties )
    {
        return DynamicPropertySetMetaClass.leftShift(this, properties);
    }
}
