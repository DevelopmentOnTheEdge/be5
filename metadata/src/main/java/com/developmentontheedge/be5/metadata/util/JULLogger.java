package com.developmentontheedge.be5.metadata.util;

import java.util.logging.Logger;

public class JULLogger implements ProcessController
{
    protected final Logger log;

    public JULLogger(Logger log)
    {
        this.log = log;
    }

    @Override
    public void setOperationName(String name)
    {
        log.info("Operation started: " + name);
    }

    @Override
    public void setProgress(double progress)
    {
        log.info("  progress: " + progress * 100 + "%");
    }

    @Override
    public void info(String msg)
    {
        log.info(msg);
    }

    @Override
    public void debug(String msg)
    {
        log.fine(msg);
    }

    @Override
    public void error(String msg)
    {
        log.severe(msg);
    }
}
