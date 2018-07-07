package com.developmentontheedge.dbms;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class ResourceTest
{
    @Test
    public void testPropertiesExists()
    {
        assertNotNull(SqlExecutor.getDefaultPropertiesFile());
    }
}
