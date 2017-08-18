package com.developmentontheedge.be5.databasemodel.groovy

import com.developmentontheedge.be5.test.AbstractProjectTest
import com.developmentontheedge.beans.BeanInfoConstants
import com.developmentontheedge.beans.DynamicProperty
import com.developmentontheedge.beans.DynamicPropertySet
import com.developmentontheedge.beans.DynamicPropertySetSupport
import com.google.common.collect.ImmutableMap;
import org.junit.Test;

import static org.junit.Assert.*

class DynamicPropertyMetaClassTest extends AbstractProjectTest
{
    @Test
    void leftShift() throws Exception
    {
        DynamicPropertySet dps = new DynamicPropertySetSupport()

        dps << [
            name: "testField",
            DISPLAY_NAME: "Test Field",
            value: 1L,
            TYPE: Long,
            GROUP_ID: 1
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

}