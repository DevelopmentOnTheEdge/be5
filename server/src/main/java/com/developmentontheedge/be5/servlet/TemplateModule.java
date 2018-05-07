package com.developmentontheedge.be5.servlet;

import com.google.inject.Scopes;
import com.google.inject.servlet.ServletModule;


public class TemplateModule extends ServletModule
{
    @Override
    protected void configureServlets()
    {
        bind(Be5TemplateFilter.class).in(Scopes.SINGLETON);
        filter("/*").through(Be5TemplateFilter.class);
        //filter("/api/*").through(Be5Filter.class);
    }
}
