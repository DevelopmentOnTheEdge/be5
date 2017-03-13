package com.developmentontheedge.be5.env;

import com.developmentontheedge.be5.api.services.impl.ProjectProviderImpl;
import com.developmentontheedge.be5.env.ServletContexts;
import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.Files;
import com.google.gson.Gson;
import org.yaml.snakeyaml.Yaml;

import javax.servlet.ServletContext;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

public enum ConfigurationProvider
{
    INSTANCE;
    
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
        ServletContext ctx = ServletContexts.getServletContext();
        ProjectProviderImpl projectProvider = new ProjectProviderImpl();
        Path projectSource = projectProvider.getPath( ctx, "be5.configPath" );
        
        if (projectSource == null)
        {
            configuration = ImmutableMap.of("components", ImmutableMap.of());
            return;
        }
        
        try
        {
            String text = Files.asCharSource(projectSource.resolve("config.yaml").toFile(), Charsets.UTF_8).read();
            configuration = (Map<String, Object>) ((Map<String, Object>) new Yaml().load(text)).get("config");
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }
    
}
