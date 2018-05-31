package com.developmentontheedge.be5.base.services.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Handler;
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

        StringBuilder sb = new StringBuilder();
        sb.append("Log root level: ").append(parentLevel);

        for (Handler handler : log.getParent().getHandlers())
        {
            sb.append("\n - ").append(handler.getClass().getName()).append(" ").append(handler.getLevel());
        }

        log.info(sb.toString());
    }

}
