package com.developmentontheedge.be5.web.impl;

import com.developmentontheedge.be5.web.Session;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.servlet.http.HttpSession;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class SessionImpl implements Session
{
    private Provider<HttpSession> raw;

    @Inject
    public SessionImpl(Provider<HttpSession> session)
    {
        this.raw = session;
    }

    @Override
    public String getSessionId()
    {
        return getRawSession().getId();
    }

    @Override
    public Object get(String name)
    {
        return getRawSession().getAttribute(name);
    }

    @Override
    public void set(String name, Object value)
    {
        getRawSession().setAttribute(name, value);
    }

    @Override
    public void remove(String name)
    {
        getRawSession().removeAttribute(name);
    }

    @Override
    public HttpSession getRawSession()
    {
        return raw.get();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<String> getAttributeNames()
    {
        return Collections.list(getRawSession().getAttributeNames());
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> getAttributes()
    {
        Map<String, Object> map = new HashMap<>();

        Enumeration<String> enumeration = getRawSession().getAttributeNames();
        while (enumeration.hasMoreElements())
        {
            String name = enumeration.nextElement();
            map.put(name, get(name));
        }

        return map;
    }

    @Override
    public void invalidate()
    {
        getRawSession().invalidate();
    }
}
