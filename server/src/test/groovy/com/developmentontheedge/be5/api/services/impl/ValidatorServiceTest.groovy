package com.developmentontheedge.be5.api.services.impl

import com.developmentontheedge.be5.api.validation.Validator
import com.developmentontheedge.be5.env.Inject
import com.developmentontheedge.be5.test.Be5ProjectTest
import com.developmentontheedge.beans.BeanInfoConstants
import com.developmentontheedge.beans.DynamicProperty
import com.developmentontheedge.beans.DynamicPropertySet
import com.developmentontheedge.beans.DynamicPropertySetSupport
import org.junit.Before
import org.junit.Ignore
import org.junit.Test

import static com.developmentontheedge.be5.api.validation.rule.BaseRule.*
import static com.developmentontheedge.be5.api.validation.rule.ValidationRules.*
import static org.junit.Assert.assertArrayEquals


class ValidatorServiceTest extends Be5ProjectTest
{
    @Inject Validator validator
    DynamicPropertySet dps

    @Before
    void initDps(){
        dps = new DynamicPropertySetSupport()
    }

    @Test
    void test() throws Exception
    {
        DynamicProperty property = new DynamicProperty("name", "Name", Long.class, 2L)
        validator.checkErrorAndCast(property)

        DynamicProperty propertyStr = new DynamicProperty("name", "Name", Long.class, "2")
        validator.checkErrorAndCast(propertyStr)
    }

    @Test
    void testMulti() throws Exception
    {
        String[] initValue = ["val", "val2"] as String[]

        def property = add(dps) {
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

        def property = add(dps) {
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
    
    @Test
    @Ignore//TODO
    void name()
    {
        DynamicProperty property = new DynamicProperty("name", "Name", String.class, 423423)
        property << [ VALIDATION_RULES: baseRule(digits) ]

        //validator.checkErrorAndCast(property)
    }

}
