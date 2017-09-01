package com.developmentontheedge.be5.api.services.impl;

import com.developmentontheedge.be5.api.Configurable;
import com.developmentontheedge.be5.api.services.Be5MainSettings;

import java.util.HashMap;
import java.util.Map;

public class Be5MainSettingsImpl implements Be5MainSettings, Configurable<Be5MainSettingsImpl.Config>
{
    private Config config = new Config();

    class Config
    {
        Map<String, Integer> cacheSizes = new HashMap<>();
    }

    @Override
    public void configure(Config config) {
        this.config = config;
    }

    @Override
    public int getCacheSize(String name)
    {
        return config.cacheSizes.getOrDefault(name, config.cacheSizes.getOrDefault("defaultSize", 1000));
    }
}
