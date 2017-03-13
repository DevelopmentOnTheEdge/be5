package com.developmentontheedge.be5.env;

import com.developmentontheedge.beans.logging.Logger;
import com.developmentontheedge.beans.logging.LoggingHandle;

public class LegacyBeanExplorerLogger implements com.developmentontheedge.be5.api.services.Logger
{
    private final LoggingHandle lh = Logger.getHandle(LegacyBeanExplorerLogger.class);

    @Override
    public void error(String message)
    {
        Logger.error(lh, message);
    }

    @Override
    public void error(Throwable t)
    {
        Logger.error(lh, t.getMessage(), t);
    }

    @Override
    public void info(String message)
    {
        Logger.info(lh, message);
    }
}
