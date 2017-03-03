package com.developmentontheedge.be5.metadata;

import java.util.Set;

import com.developmentontheedge.be5.metadata.util.ModuleUtils;

import junit.framework.TestCase;

public class LegacyBeTest extends TestCase
{
    public void testReadFeatures()
    {
        Set<String> availableFeatures = ModuleUtils.getAvailableFeatures();
        assertTrue(availableFeatures.contains( "logging" ));
        assertFalse(availableFeatures.contains( "logging_def" ));
        assertFalse(availableFeatures.contains( "logging_meta" ));
    }
    
    public void testReadModules()
    {
        Set<String> modules = ModuleUtils.getAvailableLegacyModules();
        assertTrue(modules.contains( "attributes" ));
    }
}
