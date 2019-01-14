package com.developmentontheedge.be5.model.groovy

import com.developmentontheedge.be5.groovy.meta.DynamicPropertyMetaClass
import com.developmentontheedge.be5.groovy.meta.DynamicPropertySetMetaClass
import com.developmentontheedge.be5.groovy.meta.GroovyRegister
import com.developmentontheedge.beans.DynamicProperty
import com.developmentontheedge.beans.DynamicPropertySetDecorator
import com.developmentontheedge.beans.DynamicPropertySetSupport

class RegisterMetaClass
{
    static
    {
        GroovyRegister.registerMetaClass(DynamicPropertyMetaClass.class, DynamicProperty.class);
        GroovyRegister.registerMetaClass(DynamicPropertySetMetaClass.class, DynamicPropertySetSupport.class);
        GroovyRegister.registerMetaClass(DynamicPropertySetMetaClass.class, DynamicPropertySetDecorator.class);
    }

}
