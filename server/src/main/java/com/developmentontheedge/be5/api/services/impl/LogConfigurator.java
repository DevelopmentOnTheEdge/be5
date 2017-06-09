package com.developmentontheedge.be5.api.services.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import com.developmentontheedge.be5.api.Configurable;
import com.developmentontheedge.be5.api.exceptions.Be5ErrorCode;

public class LogConfigurator implements Configurable<LogConfigurator.JulConfigPath>
{
    private Logger log = Logger.getLogger(LogConfigurator.class.getName());

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
                InputStream resourceAsStream = LogConfigurator.class.getResourceAsStream(config.path);
                if (resourceAsStream == null)
                {
                    throw Be5ErrorCode.INTERNAL_ERROR.exception("File not found: " + config.path);
                }
                LogManager.getLogManager().readConfiguration(resourceAsStream);
            }
            log = Logger.getLogger(LogConfigurator.class.getName());
            log.info("Log configured. Level: " + log.getParent().getLevel().getName() +
                    " Handlers: " + Arrays.asList(log.getParent().getHandlers()));
        }
        catch (IOException e)
        {
            throw Be5ErrorCode.INTERNAL_ERROR.rethrow(log, e);
        }
    }

}
