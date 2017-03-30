package com.developmentontheedge.be5.api.services.impl;

import java.util.Date;

import com.developmentontheedge.be5.api.services.Logger;

/**
 * This is a console logger. This means that a good logger is still required.
 * 
 * @author asko
 */
@Deprecated
public class ConsoleLogger implements Logger
{
    
    // TODO add logging system like log4j
    // TODO add any other normal logger
    
    @Override
    public void error(String message)
    {
        System.err.println(new Date() + " [ERROR]: " + message);
    }
    
    @Override
    public void error(Throwable t)
    {
        t.printStackTrace(System.err);
    }

    @Override
    public void info(String message)
    {
        System.out.println(new Date() + " [INFO]: " + message);
    }
    
}
