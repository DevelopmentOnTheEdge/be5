package com.developmentontheedge.be5.model.beans;

import com.developmentontheedge.be5.databasemodel.groovy.DynamicPropertyMetaClass;
import com.developmentontheedge.be5.databasemodel.groovy.DynamicPropertySetMetaClass;
import com.developmentontheedge.beans.DynamicProperty;
import com.developmentontheedge.beans.DynamicPropertySet;
import com.developmentontheedge.beans.DynamicPropertySetSupport;
import groovy.lang.Closure;
import groovy.lang.DelegatesTo;

import java.util.Map;


public class GDynamicPropertySetSupport extends DynamicPropertySetSupport
{
//в TypeChecked не компилится, не работает тулинг в IDEA - тогда лучше использовать $columnName - как в be3
//    public Object call(String name)
//    {
//        return super.getValue(name);
//    }

    public DynamicProperty getAt(String name)
    {
        return super.getProperty(name);
    }

    public DynamicProperty add(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = DPSAttributes.class) Closure cl)
    {
        DPSAttributes builder = getBuilder(cl);

        DynamicProperty property = new DynamicProperty(builder.getName(), builder.getTYPE());
        add(property);
        return DynamicPropertyMetaClass.leftShift(property, builder.getMap());
    }

    public DynamicProperty add(String propertyName,
                               @DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = DPSAttributes.class) Closure cl)
    {
        DPSAttributes builder = getBuilder(cl);

        DynamicProperty property = new DynamicProperty(propertyName, builder.getTYPE());
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
        DPSAttributes dpsAttributes = new DPSAttributes();
        cl.setResolveStrategy( Closure.DELEGATE_FIRST );
        cl.setDelegate( dpsAttributes );
        cl.call();
        return dpsAttributes;
    }

    /**
     * be3 синтаксис, метод без тулинга
     */
    @Deprecated
    public void putAt(String propertyName, Map<String, Object> value)
    {
        value.put( "name", propertyName );
        DynamicPropertySetMetaClass.leftShift(this, value );
    }

    @Deprecated
    public DynamicPropertySet leftShift( Map<String, Object> properties )
    {
        return DynamicPropertySetMetaClass.leftShift(this, properties);
    }
}
