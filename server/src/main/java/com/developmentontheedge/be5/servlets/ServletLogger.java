package com.developmentontheedge.be5.servlets;

import javax.servlet.ServletContext;

import com.developmentontheedge.be5.api.services.Logger;

public class ServletLogger implements Logger
{
    private ServletContext ctx;

    void setContext(ServletContext ctx)
    {
        this.ctx = ctx;
        ctx.log("Logging initialized");
    }
    
    @Override
    public void error(String message)
    {
        ctx.log("ERROR: "+message);
    }

    @Override
    public void error(Throwable t)
    {
        ctx.log("ERROR: "+t.getMessage(), t);
    }

    @Override
    public void info(String message)
    {
        ctx.log("INFO: "+message);
    }
}
