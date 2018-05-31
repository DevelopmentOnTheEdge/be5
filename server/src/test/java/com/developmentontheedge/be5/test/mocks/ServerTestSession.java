package com.developmentontheedge.be5.test.mocks;

import com.developmentontheedge.be5.web.Session;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ServerTestSession implements Session
{
    private final Map<String, Object> map = new HashMap<>();

    @Override
    public String getSessionId()
    {
        return "test session";
    }

    @Override
    public Object get(String name)
    {
        return map.get(name);
    }

    @Override
    public void set(String name, Object value)
    {
        map.put(name, value);
    }

    @Override
    public void remove(String name)
    {
        set(name, null);
    }

    @Override
    public HttpSession getRawSession()
    {
        return null;
    }

    @Override
    public List<String> getAttributeNames()
    {
        return new ArrayList<>(map.keySet());
    }

    @Override
    public void invalidate()
    {
        map.clear();
    }

    @Override
    public Map<String, Object> getAttributes()
    {
        return map;
    }
}
