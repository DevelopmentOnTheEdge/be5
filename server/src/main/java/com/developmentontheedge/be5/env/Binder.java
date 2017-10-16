package com.developmentontheedge.be5.env;

import com.developmentontheedge.be5.api.RequestPreprocessor;

import java.util.List;
import java.util.Map;

public interface Binder
{
    void configure(Map<String, Class<?>> loadedClasses, Map<Class<?>, Class<?>> bindings,
                   Map<Class<?>, Object> configurations, List<RequestPreprocessor> requestPreprocessors);

    String getInfo();
}
