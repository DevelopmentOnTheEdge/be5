package com.developmentontheedge.be5.servlet;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceServletContextListener;
import com.google.inject.servlet.ServletModule;


public class Be5GuiceServletConfig extends GuiceServletContextListener
{
    @Override
    protected Injector getInjector()
    {
        return Guice.createInjector(new ServletModule() {
            @Override
            protected void configureServlets()
            {
                filter("/*").through(Be5TemplateFilter.class);
                //filter("/api/*").through(Be5Filter.class);
            }
        });
    }
}