package com.developmentontheedge.be5.api.services;

public interface Logger
{

    /**
     * Logs a error.
     */
    public void error(String message);
    
    /**
     * Logs a error.
     */
    public void error(Throwable t);
    
    public void info(String message);
    
}
