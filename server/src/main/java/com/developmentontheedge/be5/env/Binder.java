package com.developmentontheedge.be5.env;

import java.util.Map;

public interface Binder
{
    void configure(Map<String, Class<?>> loadedClasses, Map<Class<?>, Class<?>> bindings,
                   Map<Class<?>, Object> configurations);

    String getInfo();
}
