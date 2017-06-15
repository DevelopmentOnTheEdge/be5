package com.developmentontheedge.be5.operation.databasemodel.groovy;

import com.developmentontheedge.beans.BeanInfoConstants;
import com.developmentontheedge.beans.DynamicProperty;
import com.developmentontheedge.beans.DynamicPropertySet;
import com.developmentontheedge.beans.DynamicPropertySetSupport;
import com.google.common.collect.ImmutableMap;
import org.junit.Test;

import static org.junit.Assert.*;

public class DynamicPropertySetMetaClassTest
{
    @Test
    public void leftShiftTest() throws Exception {
        DynamicPropertySet dps = new DynamicPropertySetSupport();

        DynamicPropertySetMetaClass.leftShift(dps, ImmutableMap.of(
                "name","testField",
                "DISPLAY_NAME","Test Field",
                "value",1L,
                "TYPE", java.lang.Long.class,
                "GROUP_ID",1
        ));

        DynamicProperty testField = dps.getProperty("testField");
        assertEquals("Test Field", testField.getDisplayName());
        assertEquals(java.lang.Long.class, testField.getType());
        assertEquals(1L, testField.getValue());
        assertEquals(1, testField.getAttribute(BeanInfoConstants.GROUP_ID));
    }

}