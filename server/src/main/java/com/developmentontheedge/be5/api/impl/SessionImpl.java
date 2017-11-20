package com.developmentontheedge.be5.api.impl;

import com.developmentontheedge.be5.api.Session;

import javax.servlet.http.HttpSession;
import java.util.Collections;
import java.util.List;

public class SessionImpl implements Session
{
    private HttpSession rawSession;

    SessionImpl(HttpSession session)
    {
        this.rawSession = session;
    }

    @Override
    public String getSessionId()
    {
        return rawSession.getId();
    }

    @Override
    public Object get(String name)
    {
        return rawSession.getAttribute(name);
    }

    @Override
    public void set(String name, Object value)
    {
        rawSession.setAttribute(name, value);
    }

    @Override
    public HttpSession getRawSession()
    {
        return rawSession;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<String> getAttributeNames()
    {
        return Collections.list(rawSession.getAttributeNames());
    }
}
