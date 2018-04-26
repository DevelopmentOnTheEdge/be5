package com.developmentontheedge.be5.api.services.impl;

import com.developmentontheedge.be5.inject.Inject;
import com.developmentontheedge.be5.test.Be5ProjectTest;
import org.junit.Test;

import static org.junit.Assert.*;


public class LogConfiguratorTest extends Be5ProjectTest
{
    @Inject private LogConfigurator logConfigurator;

    @Test
    public void configure()
    {
        assertEquals("/logging.properties", logConfigurator.getConfigPath().path);
    }

}