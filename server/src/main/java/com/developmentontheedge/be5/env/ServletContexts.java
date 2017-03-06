package com.developmentontheedge.be5.env;

import javax.servlet.ServletContext;

import com.developmentontheedge.be5.util.Delegator;

public class ServletContexts
{

    public static ServletContext getServletContext()
    {
        Object origServletContext = System.getProperties().get("com.beanexplorer.be5.servletContext");
        ServletContext servletContext = Delegator.on(origServletContext, ServletContext.class);
        
        return servletContext;
    }

}
