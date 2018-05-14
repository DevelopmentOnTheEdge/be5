package com.developmentontheedge.be5.api.services.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import com.developmentontheedge.be5.exceptions.Be5Exception;


public class LogConfigurator
{
    private Logger log;
    private final static String path = "/logging.properties";

    public LogConfigurator()
    {
        try
        {
            InputStream resourceAsStream = LogConfigurator.class.getResourceAsStream(path);
            if (resourceAsStream == null)
            {
                throw Be5Exception.internal("File not found: " + path);
            }
            LogManager.getLogManager().readConfiguration(resourceAsStream);

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

}
