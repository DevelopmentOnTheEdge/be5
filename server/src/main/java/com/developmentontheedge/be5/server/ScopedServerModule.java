package com.developmentontheedge.be5.server;

import com.developmentontheedge.be5.web.Request;
import com.developmentontheedge.be5.web.Response;
import com.developmentontheedge.be5.web.impl.RequestImpl;
import com.developmentontheedge.be5.web.impl.ResponseImpl;
import com.google.inject.AbstractModule;
import com.google.inject.servlet.ServletScopes;


public class ScopedServerModule extends AbstractModule
{
    @Override
    protected void configure()
    {
        bind(Request.class).to(RequestImpl.class).in(ServletScopes.REQUEST);
        bind(Response.class).to(ResponseImpl.class).in(ServletScopes.REQUEST);
    }
}
