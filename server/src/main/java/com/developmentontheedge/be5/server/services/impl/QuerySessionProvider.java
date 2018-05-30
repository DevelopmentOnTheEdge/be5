package com.developmentontheedge.be5.server.services.impl;

import com.developmentontheedge.be5.query.QuerySession;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.servlet.http.HttpSession;


public class QuerySessionProvider implements QuerySession
{
    private final Provider<HttpSession> session;

    @Inject
    public QuerySessionProvider(Provider<HttpSession> session)
    {
        this.session = session;
    }

    @Override
    public Object get(String name)
    {
        return session.get().getAttribute(name);
    }
}