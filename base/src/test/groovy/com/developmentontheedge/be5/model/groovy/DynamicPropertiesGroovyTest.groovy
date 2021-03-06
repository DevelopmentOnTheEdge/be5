package com.developmentontheedge.be5.model.groovy

import com.developmentontheedge.beans.DynamicProperty
import com.developmentontheedge.beans.DynamicPropertySet
import com.developmentontheedge.beans.DynamicPropertySetSupport
import org.junit.Test

import static com.developmentontheedge.beans.BeanInfoConstants.*
import static org.junit.Assert.*


class DynamicPropertiesGroovyTest extends RegisterMetaClass
{
    @Test
    void testSetValue()
    {
        DynamicPropertySetSupport dps = new DynamicPropertySetSupport()
        dps["property"] = "value"
        assertEquals "value", dps.getValue("property")
    }

    @Test
    void testGetValue()
    {
        DynamicPropertySetSupport dps = new DynamicPropertySetSupport()
        dps.build("property", String.class).value("value")

        assertEquals "value", dps.$property
    }

    @Test
    void testGetProperty()
    {
        DynamicPropertySetSupport dps = new DynamicPropertySetSupport()
        dps.build("property", String.class).value("value")

        assertTrue dps.property instanceof DynamicProperty
        assertEquals "value", dps.property.value
    }

    @Test
    void testGetMissingValue()
    {
        DynamicPropertySetSupport dps = new DynamicPropertySetSupport()
        assertNull dps.$property
    }

    @Test
    void testPropertyAccess()
    {
        DynamicPropertySetSupport dps = new DynamicPropertySetSupport()
        dps.build("property", String.class).value("value")
        assertEquals "value", dps._property.value
    }

    @Test
    void testGetMissingValueWithoutAccessor()
    {
        DynamicPropertySetSupport dps = new DynamicPropertySetSupport()
        boolean assertException = false
        try {
            dps.property
        }
        catch (MissingPropertyException e) {
            assertException = true
        }
        assertTrue assertException
    }

    @Test
    void testCreateProperty()
    {
        DynamicPropertySet dps = new DynamicPropertySetSupport()
        dps << [
                name            : "testProperty",
                DISPLAY_NAME    : "Тестовое свойство",
                value           : 1,
                HIDDEN          : true,
                TYPE            : java.sql.Date.class,
                TAG_LIST_ATTR   : ['A': 'a', 'B': 'b', 'C': 'c', 'D': 'd'],
                RELOAD_ON_CHANGE: true,
                RELOAD_ON_CLICK : true,
                RAW_VALUE       : true
        ]
        DynamicProperty property = dps._testProperty
        assertEquals 1, property.value
        assertEquals "testProperty", property.name
        assertEquals "Тестовое свойство", property.displayName
        assertEquals java.sql.Date.class, property.type
        assertTrue property.hidden

        final def tags = ['A': 'a', 'B': 'b', 'C': 'c', 'D': 'd']
        assertEquals tags, property.getAttribute(TAG_LIST_ATTR)
        assertTrue((boolean) property.getAttribute(RELOAD_ON_CHANGE))
        assertTrue((boolean) property.getAttribute(RELOAD_ON_CLICK))
        assertTrue((boolean) property.getAttribute(RAW_VALUE))
    }

    @Test
    void testAddProperty()
    {
        DynamicPropertySet dps = [a: "a", b: "b"] as DynamicPropertySetSupport
        DynamicProperty property = new DynamicProperty("d", String, "d")
        assertTrue((dps << property).hasProperty("d"))
        assertTrue dps.hasProperty("d")
    }

    @Test
    void testGetAttribute()
    {
        DynamicPropertySetSupport dps = new DynamicPropertySetSupport()
        dps.build("test", String.class).attr(INPUT_SIZE_ATTR, "10")

        assertEquals dps._test.attr[INPUT_SIZE_ATTR], "10"
    }

    @Test
    void testGetAttributeStringConstant()
    {
        DynamicPropertySetSupport dps = new DynamicPropertySetSupport()
        dps.build("test", String.class).attr(INPUT_SIZE_ATTR, "10")

        assertEquals dps._test.attr[INPUT_SIZE_ATTR], "10"
        assertEquals dps._test.attr.INPUT_SIZE_ATTR, "10"
    }

    @Test
    void testSetAttribute()
    {
        def DynamicPropertySetSupport dps = new DynamicPropertySetSupport()
        dps.build("test", String.class)

        dps._test.attr[INPUT_SIZE_ATTR] = "10"
        assertEquals dps._test.getAttribute(INPUT_SIZE_ATTR), "10"
    }

    @Test
    void testSetAttributeStringConstant()
    {
        def DynamicPropertySetSupport dps = new DynamicPropertySetSupport()
        dps.build("test", String.class)

        dps._test.attr["INPUT_SIZE_ATTR"] = "10"
        assertEquals dps._test.getAttribute(INPUT_SIZE_ATTR), "10"
    }

    @Test
    void testGetAt()
    {
        DynamicPropertySet dps = new DynamicPropertySetSupport()
        dps << [
                name : "testProperty",
                value: 1
        ]
        assertEquals 1, dps['testProperty']

        assertEquals null, dps['$testProperty']
        assertEquals null, dps['_testProperty']
    }

    @Test
    void testGStringImplConvertToString()
    {
        DynamicPropertySet dps = new DynamicPropertySetSupport()
        def test = "1"
        dps << [
                name : "property1",
                value: "test value ${test}"
        ]
        assertEquals String.class, dps.getValue("property1").getClass()

        dps["property2"] = "test value ${test}"
        assertEquals String.class, dps.getValue("property2").getClass()
    }

    @Test
    void testGetAtWithoutAccessor()
    {
        def dps = new DynamicPropertySetSupport()
        dps << [
                name : "testProperty",
                value: 1
        ]
        assertEquals 1, dps["testProperty"].value
    }


    @Test
    void testDynamicPropertySetPlus()
    {
        def dps1 = [a: "a", b: "b", c: "c"] as DynamicPropertySetSupport
        def dps2 = [d: "d", e: "e", f: "f"] as DynamicPropertySetSupport

        DynamicPropertySetSupport dps3 = dps1 + dps2
        assertTrue dps1.size() == 3
        assertTrue dps2.size() == 3
        [a: "a", b: "b", c: "c", d: "d", e: "e", f: "f"].each({ a, b ->
            assertTrue dps3.getValue(a) == b
        })
    }


    @Test
    void testSetWith()
    {
        DynamicPropertySet dps = new DynamicPropertySetSupport()
        dps << [name: "testProperty", value: 1]
        dps << [name: "testProperty2", value: 1]
        dps.with {
            testProperty = 2
            testProperty2 = 2
        }
        assertEquals 2, dps.getValue("testProperty")
        assertEquals 2, dps.getValue("testProperty2")
    }

}
