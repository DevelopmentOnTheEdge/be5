package com.developmentontheedge.be5.server;

import com.developmentontheedge.be5.query.QuerySession;
import com.developmentontheedge.be5.server.services.impl.QuerySessionImpl;
import com.developmentontheedge.be5.web.Request;
import com.developmentontheedge.be5.web.Response;
import com.developmentontheedge.be5.web.Session;
import com.developmentontheedge.be5.web.impl.RequestImpl;
import com.developmentontheedge.be5.web.impl.ResponseImpl;
import com.developmentontheedge.be5.web.impl.SessionImpl;
import com.google.inject.servlet.ServletModule;
import com.google.inject.servlet.ServletScopes;


public class ScopedServerModule extends ServletModule
{
    @Override
    protected void configureServlets()
    {
        bind(QuerySession.class).to(QuerySessionImpl.class).in(ServletScopes.SESSION);
        bind(Session.class).to(SessionImpl.class).in(ServletScopes.SESSION);
        bind(Request.class).to(RequestImpl.class).in(ServletScopes.REQUEST);
        bind(Response.class).to(ResponseImpl.class).in(ServletScopes.REQUEST);
    }
}
