package com.developmentontheedge.be5.api.impl;

import com.developmentontheedge.be5.api.Session;

import javax.servlet.http.HttpSession;

public class SessionImpl implements Session
{
    private HttpSession session;

    SessionImpl(HttpSession session)
    {
        this.session = session;
    }

    @Override
    public String getSessionId()
    {
        return session.getId();
    }

    @Override
    public Object get(String name)
    {
        return session.getAttribute(name);
    }

    @Override
    public void set(String name, Object value)
    {
        session.setAttribute(name, value);
    }
}
