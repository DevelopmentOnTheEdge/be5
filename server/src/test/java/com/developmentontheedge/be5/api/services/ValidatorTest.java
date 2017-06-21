package com.developmentontheedge.be5.api.services;

import com.developmentontheedge.be5.test.AbstractProjectTest;
import com.developmentontheedge.beans.DynamicProperty;
import org.junit.Test;

import static com.developmentontheedge.be5.api.services.Validator.Status.SUCCESS;
import static com.developmentontheedge.be5.api.services.Validator.Status.ERROR;
import static org.junit.Assert.assertEquals;

public class ValidatorTest extends AbstractProjectTest
{
    private static final Validator validator = injector.get(Validator.class);

    @Test
    public void test() throws Exception
    {
        DynamicProperty property = new DynamicProperty("name", "Name", Long.class, 2L);
        assertEquals(SUCCESS, validator.checkErrorAndCast(property));

        DynamicProperty propertyStr = new DynamicProperty("name", "Name", Long.class, "2");
        assertEquals(SUCCESS, validator.checkErrorAndCast(propertyStr));
    }

    @Test
    public void testError() throws Exception
    {
        DynamicProperty property = new DynamicProperty("name", "Name", Long.class, "a");
        assertEquals(ERROR, validator.checkErrorAndCast(property));
        assertEquals("Error, value must be a java.lang.Long", property.getStringAttribute("message"));
    }

    @Test
    public void testString() throws Exception
    {
        DynamicProperty property = new DynamicProperty("name", "Name", String.class, 2);
        assertEquals(ERROR, validator.checkErrorAndCast(property));
        assertEquals("Error, value must be a java.lang.String", property.getStringAttribute("message"));
    }


}