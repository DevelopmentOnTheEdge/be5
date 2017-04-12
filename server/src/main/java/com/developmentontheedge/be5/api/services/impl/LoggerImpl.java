package com.developmentontheedge.be5.api.services.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.LogManager;

import com.developmentontheedge.be5.api.Configurable;
import com.developmentontheedge.be5.api.exceptions.Be5ErrorCode;
import com.developmentontheedge.be5.api.services.Logger;

public class LoggerImpl implements Logger, Configurable<LoggerImpl.JulConfigPath>
{
    private java.util.logging.Logger log = java.util.logging.Logger.getLogger(LoggerImpl.class.getName());

    class JulConfigPath{
        String path;
    }

    @Override
    public void configure(JulConfigPath config)
    {
        try
        {
            if(config != null)
            {
                InputStream resourceAsStream = LoggerImpl.class.getResourceAsStream(config.path);
                if (resourceAsStream == null)
                {
                    throw Be5ErrorCode.INTERNAL_ERROR.exception("File not found: " + config.path);
                }
                LogManager.getLogManager().readConfiguration(resourceAsStream);
            }
            log = java.util.logging.Logger.getLogger(LoggerImpl.class.getName());
            log.info("Log configured. Level: " + log.getParent().getLevel().getName() +
                    " Handlers: " + Arrays.asList(log.getParent().getHandlers()));
        }
        catch (IOException e)
        {
            throw Be5ErrorCode.INTERNAL_ERROR.rethrow(log, e);
        }
    }

    @Override
    public void error(String message)
    {
        log.severe(message);
    }

    @Override
    public void error(Throwable t)
    {
        log.log(Level.SEVERE, "",t);
    }

    @Override
    public void info(String message)
    {
        log.info(message);
    }

}
