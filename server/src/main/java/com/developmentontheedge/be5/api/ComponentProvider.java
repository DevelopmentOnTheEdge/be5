package com.developmentontheedge.be5.api;

/**
 * Classes cache: componentId->class.
 */
public interface ComponentProvider
{
    Class<?> get(String componentId);

    void put(String componentId, Class<?> value);
}
