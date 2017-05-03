package com.developmentontheedge.be5.env;

import com.developmentontheedge.be5.api.exceptions.Be5ErrorCode;
import com.developmentontheedge.be5.api.exceptions.Be5Exception;
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

import static com.developmentontheedge.be5.env.ServerModuleLoader.CONTEXT_FILE;

public enum ConfigurationProvider
{
    INSTANCE;

    private static final Logger log = Logger.getLogger(ConfigurationProvider.class.getName());
    
    private Map<String, Object> configuration = null;
    
    public <T> T getConfiguration(Class<T> configClass, String collection, String id)
    {
        if (configuration == null)
        {
            throw Be5ErrorCode.INTERNAL_ERROR.exception("Call loadConfiguration first.");
        }
        
        @SuppressWarnings("unchecked")
        Map<String, Object> configurations = (Map<String, Object>) configuration.get(collection);

        if(configurations == null){
            log.warning("Module in " + collection + " '" + id + "' not configured.");
            return null;
        }

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


    public void loadConfiguration()
    {
        configuration = new HashMap<>();
        try
        {
            ArrayList<URL> urls = Collections.list(getClass().getClassLoader().getResources(CONTEXT_FILE));

            for (URL url : urls)
            {
                try(BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream(), "utf-8")))
                {
                    loadModuleConfiguration(reader);
                }
            }
        }
        catch (IOException e)
        {
            throw Be5Exception.internal(e,"Can't load server modules configuration.");
        }
    }

    @SuppressWarnings("unchecked")
    void loadModuleConfiguration(BufferedReader reader)
    {
        if (configuration == null)
        {
            throw Be5ErrorCode.INTERNAL_ERROR.exception("Call loadConfiguration first.");
        }
        Map<String, Object> moduleConfiguration = (Map<String, Object>) ((Map<String, Object>) new Yaml().load(reader)).get("config");
        //TODO several config check, test
        if(moduleConfiguration != null)configuration.putAll(moduleConfiguration);
    }

}
