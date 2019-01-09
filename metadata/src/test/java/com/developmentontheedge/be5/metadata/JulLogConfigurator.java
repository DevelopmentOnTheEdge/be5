package com.developmentontheedge.be5.metadata;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class JulLogConfigurator
{
    private static final Logger log = Logger.getLogger(JulLogConfigurator.class.getName());
    private static final String path = "/logging.properties";

    public static void config()
    {
        try (InputStream resourceAsStream = JulLogConfigurator.class.getResourceAsStream(path))
        {
            if (resourceAsStream == null)
            {
                log.info("File not found: " + path + ", log not configured.");
            }
            else
            {
                LogManager.getLogManager().readConfiguration(resourceAsStream);
            }
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
        String parentLevel = log.getParent().getLevel() != null ? log.getParent().getLevel().getName() : "null";
        log.info("Log root level: " + parentLevel);
    }
}
