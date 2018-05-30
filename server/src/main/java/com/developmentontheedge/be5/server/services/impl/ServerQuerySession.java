package com.developmentontheedge.be5.server.services.impl;

import com.developmentontheedge.be5.query.QuerySession;
import com.google.inject.servlet.SessionScoped;

import javax.inject.Inject;
import javax.servlet.http.HttpSession;


@SessionScoped
public class ServerQuerySession implements QuerySession
{
    private final HttpSession session;

    @Inject
    public ServerQuerySession(HttpSession session)
    {
        this.session = session;
    }

    @Override
    public Object get(String name)
    {
        return session.getAttribute(name);
    }
}