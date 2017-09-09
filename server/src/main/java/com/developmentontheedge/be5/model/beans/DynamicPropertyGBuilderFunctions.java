package com.developmentontheedge.be5.model.beans;

import com.developmentontheedge.be5.databasemodel.groovy.DynamicPropertyMetaClass;
import com.developmentontheedge.beans.DynamicProperty;
import groovy.lang.Closure;
import groovy.lang.DelegatesTo;

public class DynamicPropertyGBuilderFunctions
{
    public DynamicProperty of(@DelegatesTo(strategy = Closure.DELEGATE_ONLY, value = DPSAttributesFunctions.class)
                                     final Closure cl)
    {
        DPSAttributesFunctions builder = new DPSAttributesFunctions();
        Closure code = cl.rehydrate(builder, this, this);
        code.setResolveStrategy(Closure.DELEGATE_ONLY);
        code.call();

        DynamicProperty dynamicProperty = new DynamicProperty(builder.getName(), builder.getTYPE());
        return DynamicPropertyMetaClass.leftShift(dynamicProperty, builder.getMap());
    }

}
