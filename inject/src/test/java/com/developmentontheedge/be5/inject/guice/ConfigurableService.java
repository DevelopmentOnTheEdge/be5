package com.developmentontheedge.be5.inject.guice;

import com.developmentontheedge.be5.inject.Configurable;
import com.developmentontheedge.be5.inject.services.TestService;


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

    public static class Config
    {
        String url;
        int count;

        public Config(String url, int count)
        {
            this.url = url;
            this.count = count;
        }
    }

}
