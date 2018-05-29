package com.developmentontheedge.be5.web.impl;

import com.developmentontheedge.be5.web.Session;

import javax.servlet.http.HttpSession;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


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
    public void remove(String name)
    {
        set(name, null);
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

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> getAllAttributes()
    {
        Map<String, Object> map = new HashMap<>();

        Enumeration<String> enumeration = rawSession.getAttributeNames();
        while (enumeration.hasMoreElements()) {
            String name = enumeration.nextElement();
            map.put(name, get(name));
        }

        return map;
    }

    @Override
    public void invalidate()
    {
        rawSession.invalidate();
    }
}
