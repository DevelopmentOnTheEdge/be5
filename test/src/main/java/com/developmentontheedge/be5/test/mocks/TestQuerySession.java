package com.developmentontheedge.be5.test.mocks;

import com.developmentontheedge.be5.query.QuerySession;

import java.util.HashMap;
import java.util.Map;


public class TestQuerySession implements QuerySession
{
    public static final Map<String, Object> map = new HashMap<>();

    @Override
    public Object get(String name)
    {
        return map.get(name);
    }
}
