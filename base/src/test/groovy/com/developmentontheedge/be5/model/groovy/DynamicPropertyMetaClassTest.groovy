package com.developmentontheedge.be5.model.groovy

import com.developmentontheedge.be5.groovy.meta.DynamicPropertyMetaClass
import com.developmentontheedge.beans.BeanInfoConstants
import com.developmentontheedge.beans.DynamicProperty
import com.developmentontheedge.beans.DynamicPropertySet
import com.developmentontheedge.beans.DynamicPropertySetSupport
import com.google.common.collect.ImmutableMap
import org.junit.Test

import static org.junit.Assert.assertEquals

class DynamicPropertyMetaClassTest extends RegisterMetaClass
{
    @Test
    void leftShift()
    {
        DynamicPropertySet dps = new DynamicPropertySetSupport()

        dps << [
                name        : "testField",
                DISPLAY_NAME: "Test Field",
                value       : 1L,
                TYPE        : Long,
                GROUP_ID    : 1
        ]

        DynamicPropertyMetaClass.leftShift(dps.getProperty("testField"), ImmutableMap.of(
                "READ_ONLY", true
        ))

        DynamicProperty testField = dps.getProperty("testField")

        assertEquals "Test Field", testField.getDisplayName()
        assertEquals Long.class, testField.getType()
        assertEquals 1L, testField.getValue()
        assertEquals 1, testField.getAttribute(BeanInfoConstants.GROUP_ID)

        assertEquals true, testField.getAttribute(BeanInfoConstants.READ_ONLY)
    }

    @Test
    void leftShiftChangeValue()
    {
        DynamicPropertySet dps = new DynamicPropertySetSupport()

        dps << [
                name : "testField",
                value: 1L
        ]

        DynamicPropertyMetaClass.leftShift(dps.getProperty("testField"), ImmutableMap.of(
                "value", 2L
        ))

        assertEquals 2L, dps.getProperty("testField").getValue()
    }

    @Test
    void setProperty()
    {
        DynamicPropertySet dps = new DynamicPropertySetSupport()

        dps.testField = [
                DISPLAY_NAME: "Test Field",
                value       : 1L,
                TYPE        : Long,
                GROUP_ID    : 1
        ]

        DynamicProperty testField = dps.getProperty("testField")

        assertEquals "Test Field", testField.getDisplayName()
        assertEquals Long.class, testField.getType()
        assertEquals 1L, testField.getValue()
        assertEquals 1, testField.getAttribute(BeanInfoConstants.GROUP_ID)
    }

    @Test
    void updateValue()
    {
        DynamicPropertySet dps = new DynamicPropertySetSupport()

        dps.testField = [ DISPLAY_NAME: "Test Field" ]

        DynamicProperty testField = dps.getProperty("testField")
        assertEquals null, testField.getValue()
        dps.testField = [ value: "testValue" ]
        assertEquals "testValue", testField.getValue()
        dps.testField = [ DISPLAY_NAME: "Test Field2" ]
        assertEquals "testValue", testField.getValue()

        dps.testField = [ value: null ]
        assertEquals null, testField.getValue()
    }

    @Test
    void setPropertyNull()
    {
        DynamicPropertySet dps = new DynamicPropertySetSupport()

        dps.testField = null

        DynamicProperty testField = dps.getProperty("testField")

        assertEquals "testField", testField.getDisplayName()
        assertEquals String.class, testField.getType()
        assertEquals null, testField.getValue()
    }
}
