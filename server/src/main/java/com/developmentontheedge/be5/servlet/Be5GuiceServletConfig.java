package com.developmentontheedge.be5.servlet;

import com.developmentontheedge.be5.ServerModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Scopes;
import com.google.inject.servlet.GuiceServletContextListener;
import com.google.inject.servlet.ServletModule;


public class Be5GuiceServletConfig extends GuiceServletContextListener
{
    @Override
    protected Injector getInjector()
    {
        return Guice.createInjector(
                new ServerModule(),
                new TemplateModule()
        );
    }
}