package com.developmentontheedge.be5.api.services;

import com.developmentontheedge.be5.test.AbstractProjectTest;
import com.developmentontheedge.beans.DynamicProperty;
import org.junit.Test;

public class ValidatorTest extends AbstractProjectTest
{
    private static final Validator validator = injector.get(Validator.class);

    @Test
    public void test() throws Exception
    {
        DynamicProperty property = new DynamicProperty("name", "Name", Long.class, 2L);
        validator.checkErrorAndCast(property);

        DynamicProperty propertyStr = new DynamicProperty("name", "Name", Long.class, "2");
        validator.checkErrorAndCast(propertyStr);
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


}