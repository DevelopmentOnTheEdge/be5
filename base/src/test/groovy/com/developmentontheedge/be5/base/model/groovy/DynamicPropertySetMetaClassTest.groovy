package com.developmentontheedge.be5.base.model.groovy

import com.developmentontheedge.beans.BeanInfoConstants
import com.developmentontheedge.beans.DynamicProperty
import com.developmentontheedge.beans.DynamicPropertySet
import com.developmentontheedge.beans.DynamicPropertySetSupport
import com.google.common.collect.ImmutableMap
import org.junit.Test

import static org.junit.Assert.assertEquals


class DynamicPropertySetMetaClassTest extends RegisterMetaClass
{
    @Test
    void leftShiftTest()
    {
        DynamicPropertySet dps = new DynamicPropertySetSupport();

        DynamicPropertySetMetaClass.leftShift(dps, ImmutableMap.of(
                "name", "testField",
                "DISPLAY_NAME", "Test Field",
                "value", 1L,
                "TYPE", Long.class,
                "GROUP_ID", 1
        ))

        DynamicPropertySet dynamicProperties = DynamicPropertySetMetaClass.leftShift(dps, ImmutableMap.of(
                "name", "testField",
                "GROUP_NAME", "Test group name"
        ))

        assertEquals dynamicProperties, dps

        DynamicProperty testField = dps.getProperty("testField")
        assertEquals("Test Field", testField.getDisplayName())
        assertEquals(Long.class, testField.getType())
        assertEquals(1L, testField.getValue())
        assertEquals(1, testField.getAttribute(BeanInfoConstants.GROUP_ID))
        assertEquals("Test group name", testField.getAttribute(BeanInfoConstants.GROUP_NAME))
    }
}
