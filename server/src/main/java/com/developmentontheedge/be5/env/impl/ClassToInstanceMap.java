package com.developmentontheedge.be5.env.impl;

import java.util.HashMap;
import java.util.Map;

class ClassToInstanceMap
{

    private final Map<Class<?>, Object> map;

    ClassToInstanceMap()
    {
        this.map = new HashMap<>();
    }

    public <T> void put(Class<T> clazz, T value)
    {
        assert clazz.isInstance(value);
        map.put(clazz, value);
    }

    public <T> T get(Class<T> clazz)
    {
        return clazz.cast(map.get(clazz));
    }

}
