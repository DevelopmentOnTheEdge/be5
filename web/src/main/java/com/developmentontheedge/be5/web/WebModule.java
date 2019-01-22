package com.developmentontheedge.be5.web;

import com.developmentontheedge.be5.web.impl.RequestImpl;
import com.developmentontheedge.be5.web.impl.ResponseImpl;
import com.google.inject.servlet.ServletModule;
import com.google.inject.servlet.ServletScopes;


public class WebModule extends ServletModule
{
    @Override
    protected void configureServlets()
    {
        bind(Request.class).to(RequestImpl.class).in(ServletScopes.REQUEST);
        bind(Response.class).to(ResponseImpl.class).in(ServletScopes.REQUEST);
    }
}
