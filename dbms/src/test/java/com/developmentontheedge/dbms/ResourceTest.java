package com.developmentontheedge.dbms;

import static org.junit.Assert.*;

import org.junit.Test;

public class ResourceTest
{
    @Test
    public void testPropertiesExists()
    {
        assertNotNull( SqlExecutor.getDefaultPropertiesFile() );
    }
}
