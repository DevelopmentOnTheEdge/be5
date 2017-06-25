package com.developmentontheedge.be5.databasemodel;

import com.developmentontheedge.be5.databasemodel.groovy.DynamicPropertyMetaClass;
import com.developmentontheedge.be5.databasemodel.groovy.DynamicPropertySetMetaClass;
import com.developmentontheedge.beans.DynamicProperty;
import com.developmentontheedge.beans.DynamicPropertySetSupport;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DatabaseModelTest
{

    @Test
    public void databaseModel() throws Exception
    {
        assertEquals(DynamicPropertyMetaClass.class,
                InvokerHelper.getMetaRegistry().getMetaClass(DynamicProperty.class).getClass());

        assertEquals(DynamicPropertySetMetaClass.class,
                InvokerHelper.getMetaRegistry().getMetaClass(DynamicPropertySetSupport.class).getClass());

    }

}
