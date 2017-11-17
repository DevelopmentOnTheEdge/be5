package com.developmentontheedge.be5.test;

import com.developmentontheedge.be5.api.Session;

import java.util.HashMap;
import java.util.Map;


public class TestSession implements Session
{
    private Map<String, Object> map = new HashMap<>();

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
}
