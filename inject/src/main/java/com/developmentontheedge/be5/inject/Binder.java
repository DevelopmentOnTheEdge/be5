package com.developmentontheedge.be5.inject;

import java.util.List;
import java.util.Map;


public interface Binder
{
    void configure(Map<String, Class<?>> loadedClasses, Map<Class<?>, Class<?>> bindings,
                   Map<Class<?>, Object> configurations, List<Class<?>> requestPreprocessors);

    String getInfo();
}
