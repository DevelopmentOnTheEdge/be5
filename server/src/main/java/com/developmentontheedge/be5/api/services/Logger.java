package com.developmentontheedge.be5.api.services;

public interface Logger
{

    /**
     * Logs a error.
     */
    void error(String message);
    
    /**
     * Logs a error.
     */
    void error(Throwable t);
    
    void info(String message);
    
}
