package com.developmentontheedge.be5.server.servlet;

import com.google.inject.servlet.ServletModule;


public class TemplateModule extends ServletModule
{
    @Override
    protected void configureServlets()
    {
        filter("/*").through(TemplateFilter.class);
    }
}
