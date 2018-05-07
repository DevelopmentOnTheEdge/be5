package com.developmentontheedge.be5.api.services.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import com.developmentontheedge.be5.inject.Configurable;
import com.developmentontheedge.be5.exceptions.Be5Exception;


public class LogConfigurator implements Configurable<LogConfigurator.JulConfigPath>
{
    private Logger log = Logger.getLogger(LogConfigurator.class.getName());

    class JulConfigPath{
        String path;
    }

    private JulConfigPath configPath;

    @Override
    public void configure(JulConfigPath configPath)
    {
        this.configPath = configPath;
        try
        {
            if(configPath != null)
            {
                InputStream resourceAsStream = LogConfigurator.class.getResourceAsStream(configPath.path);
                if (resourceAsStream == null)
                {
                    throw Be5Exception.internal("File not found: " + configPath.path);
                }
                LogManager.getLogManager().readConfiguration(resourceAsStream);
            }
            log = Logger.getLogger(LogConfigurator.class.getName());
            String level = log.getLevel() != null ? log.getLevel().getName() :
                    log.getParent().getLevel() != null ? log.getParent().getLevel().getName() : "null";

            log.info("Log configured. Level: " + level +
                    " Handlers: " + Arrays.asList(log.getParent().getHandlers()));
        }
        catch (IOException e)
        {
            throw Be5Exception.internal(log, e);
        }
    }

    public JulConfigPath getConfigPath()
    {
        return configPath;
    }
}
