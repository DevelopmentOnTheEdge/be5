package com.developmentontheedge.be5.inject.services;

import com.developmentontheedge.be5.inject.Configurable;

public class ConfigurableService implements Configurable<ConfigurableService.Config>
{
    private final TestService testService;

    public ConfigurableService(TestService testService)
    {
        this.testService = testService;
    }

    @Override
    public void configure(Config config)
    {
        testService.call(config.url + " " + config.count);
    }

    class Config
    {
        int count;
        String url;
    }

}
