package com.developmentontheedge.be5.server.services.impl;

import javax.inject.Inject;

import com.developmentontheedge.be5.base.services.impl.LogConfigurator;
import com.developmentontheedge.be5.test.ServerBe5ProjectTest;
import org.junit.Test;


public class LogConfiguratorTest extends ServerBe5ProjectTest
{
    @Inject private LogConfigurator logConfigurator;

    @Test//TODO
    public void configure()
    {
        //assertEquals("/logging.properties", logConfigurator.getConfigPath().path);
    }

}