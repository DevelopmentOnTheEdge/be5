package com.developmentontheedge.be5.env;

import com.developmentontheedge.be5.api.exceptions.impl.Be5ErrorCode;
import com.google.gson.Gson;
import org.yaml.snakeyaml.Yaml;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public enum ConfigurationProvider
{
    INSTANCE;

    private static final Logger log = Logger.getLogger(ConfigurationProvider.class.getName());
    
    private Map<String, Object> configuration;
    
    public <T> T loadConfiguration(Class<T> configClass, String collection, String id)
    {
        if (configuration == null)
        {
            loadConfiguration();
        }
        
        @SuppressWarnings("unchecked")
        Map<String, Object> configurations = (Map<String, Object>) configuration.get(collection);
        Object config = configurations.get(id);
        
        if (config == null)
        {
            try
            {
                return configClass.newInstance();
            }
            catch (InstantiationException | IllegalAccessException e)
            {
                throw new RuntimeException();
            }
        }
        
        String componentConfigJson = new Gson().toJson(config);
        
        return new Gson().fromJson(componentConfigJson, configClass);
    }

    @SuppressWarnings("unchecked")
    private void loadConfiguration()
    {
        try
        {
            ArrayList<URL> urls = Collections.list((ConfigurationProvider.class).getClassLoader().getResources("config.yaml"));
            configuration = new HashMap<>();

            for (URL url : urls)
            {
                BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream(), "utf-8"));
                Map<String, Object> moduleConfiguration = (Map<String, Object>) ((Map<String, Object>) new Yaml().load(reader)).get("config");

                //TODO several config check, test
                configuration.putAll(moduleConfiguration);
            }
        }
        catch (IOException e)
        {
            throw Be5ErrorCode.INTERNAL_ERROR.rethrow(log, e);
        }
    }
    
}
