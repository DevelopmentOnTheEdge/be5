package com.developmentontheedge.be5.api.services.impl

import com.developmentontheedge.be5.api.validation.Validator
import com.developmentontheedge.be5.env.Inject
import com.developmentontheedge.be5.test.Be5ProjectTest
import com.developmentontheedge.beans.BeanInfoConstants
import com.developmentontheedge.beans.DynamicProperty
import org.junit.Test

import static org.junit.Assert.assertArrayEquals


class ValidatorServiceTest extends Be5ProjectTest
{
    @Inject Validator validator

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
        String[] value = ["val", "val2"] as String[]
        DynamicProperty property = new DynamicProperty("name", "Name", String.class, value)
        property.setAttribute(BeanInfoConstants.MULTIPLE_SELECTION_LIST, true)

        validator.checkErrorAndCast(property)

        assertArrayEquals(value, (Object[])property.getValue())
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
    void name()
    {
        //parameters._account.attributes[ Validation.RULES_ATTR ] = [ "number": "Please enter only digits." ]
        DynamicProperty property = new DynamicProperty("name", "Name", String.class, 423423)
        property << [ VALIDATION_RULES: [ "number": "Please enter only digits." ] ]


        //validator.checkErrorAndCast(property)
    }
}
