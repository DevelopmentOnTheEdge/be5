package com.developmentontheedge.be5.base.services.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import com.developmentontheedge.be5.base.exceptions.Be5Exception;


public class LogConfigurator
{
    private final static Logger log = Logger.getLogger(LogConfigurator.class.getName());
    private final static String path = "/logging.properties";

    public LogConfigurator()
    {
        try
        {
            InputStream resourceAsStream = LogConfigurator.class.getResourceAsStream(path);
            if (resourceAsStream == null)
            {
                log.info("File not found: " + path + ", log not configured.");
                return;
            }

            LogManager.getLogManager().readConfiguration(resourceAsStream);
        }
        catch (IOException e)
        {
            throw Be5Exception.internal(e);
        }

        String parentLevel = log.getParent().getLevel() != null ? log.getParent().getLevel().getName() : "null";

        log.info("Log root level: " + parentLevel);
    }

}