package com.developmentontheedge.be5.api.services

import com.developmentontheedge.be5.api.validation.Validator
import com.developmentontheedge.be5.env.Inject
import com.developmentontheedge.be5.model.beans.GDynamicPropertySetSupport
import com.developmentontheedge.be5.test.Be5ProjectTest
import com.developmentontheedge.beans.BeanInfoConstants
import com.developmentontheedge.beans.DynamicProperty
import org.junit.Before
import org.junit.Ignore
import org.junit.Test

import static com.developmentontheedge.be5.api.validation.rule.BaseRule.*
import static com.developmentontheedge.be5.api.validation.rule.ValidationRules.*
import static org.junit.Assert.assertArrayEquals
import static org.junit.Assert.assertEquals


class ValidatorServiceTest extends Be5ProjectTest
{
    @Inject Validator validator
    GDynamicPropertySetSupport dps

    @Before
    void initDps()
    {
        dps = new GDynamicPropertySetSupport()
    }

    @Test
    void test() throws Exception
    {
        DynamicProperty property = new DynamicProperty("name", "Name", Long.class, 2L)
        validator.checkErrorAndCast(property)
        assertEquals 2L, property.getValue()

        DynamicProperty propertyStr = new DynamicProperty("name", "Name", Long.class, "2")
        validator.checkErrorAndCast(propertyStr)
        assertEquals 2L, property.getValue()
    }

    @Test
    void testMulti() throws Exception
    {
        String[] initValue = ["val", "val2"] as String[]

        def property = dps.add {
            name          = "name"
            TYPE          = String
            value         = initValue
            MULTIPLE_SELECTION_LIST = true
        }
        validator.checkErrorAndCast(dps)

        assertArrayEquals(initValue, (Object[])property.getValue())
    }

    @Test(expected = IllegalArgumentException.class)
    void testMultiCanNotBeNull() throws Exception
    {
        String[] initValue = [] as String[]

        def property = dps.add {
            name          = "name"
            TYPE          = String
            value         = initValue
            MULTIPLE_SELECTION_LIST = true
        }
        validator.checkErrorAndCast(dps)

        assertArrayEquals(initValue, (Object[])property.getValue())
    }

    @Test
    void testMultiLong() throws Exception
    {
        String[] value = ["1", "3"] as String[]
        DynamicProperty property = new DynamicProperty("name", "Name", Long.class, value)
        property.setAttribute(BeanInfoConstants.MULTIPLE_SELECTION_LIST, true)

        validator.checkErrorAndCast(property)

        assertArrayEquals([1L, 3L] as Long[], (Object[])property.getValue())
    }

    @Test(expected = NumberFormatException.class)
    void testError() throws Exception
    {
        DynamicProperty property = new DynamicProperty("name", "Name", Long.class, "a")
        validator.checkErrorAndCast(property)
    }

    @Test(expected = IllegalArgumentException.class)
    void testString() throws Exception
    {
        DynamicProperty property = new DynamicProperty("name", "Name", String.class, 2)
        validator.checkErrorAndCast(property)
    }

    @Ignore//TODO
    @Test(expected = NumberFormatException.class)
    void name()
    {
        DynamicProperty property = new DynamicProperty("name", "Name", String.class, "a")
        property << [ VALIDATION_RULES: baseRule(digits) ]

        validator.checkErrorAndCast(property)
    }

}