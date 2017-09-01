package com.developmentontheedge.be5.api.services;

import com.developmentontheedge.be5.api.validation.Validator;
import com.developmentontheedge.be5.env.Inject;
import com.developmentontheedge.be5.test.Be5ProjectTest;
import com.developmentontheedge.beans.BeanInfoConstants;
import com.developmentontheedge.beans.DynamicProperty;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;

public class ValidatorTest extends Be5ProjectTest
{
    @Inject private Validator validator;

    @Test
    public void test() throws Exception
    {
        DynamicProperty property = new DynamicProperty("name", "Name", Long.class, 2L);
        validator.checkErrorAndCast(property);

        DynamicProperty propertyStr = new DynamicProperty("name", "Name", Long.class, "2");
        validator.checkErrorAndCast(propertyStr);
    }

    @Test
    public void testMulti() throws Exception
    {
        String[] value = {"val", "val2"};
        DynamicProperty property = new DynamicProperty("name", "Name", String.class, value);
        property.setAttribute(BeanInfoConstants.MULTIPLE_SELECTION_LIST, true);

        validator.checkErrorAndCast(property);

        assertArrayEquals(value, (Object[])property.getValue());
    }

    @Test
    public void testMultiLong() throws Exception
    {
        String[] value = {"1", "3"};
        DynamicProperty property = new DynamicProperty("name", "Name", Long.class, value);
        property.setAttribute(BeanInfoConstants.MULTIPLE_SELECTION_LIST, true);

        validator.checkErrorAndCast(property);

        assertArrayEquals(new Object[]{1L, 3L}, (Object[])property.getValue());
    }

    @Test(expected = NumberFormatException.class)
    public void testError() throws Exception
    {
        DynamicProperty property = new DynamicProperty("name", "Name", Long.class, "a");
        validator.checkErrorAndCast(property);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testString() throws Exception
    {
        DynamicProperty property = new DynamicProperty("name", "Name", String.class, 2);
        validator.checkErrorAndCast(property);
    }

    //add test setError(DynamicProperty property, String message)
}