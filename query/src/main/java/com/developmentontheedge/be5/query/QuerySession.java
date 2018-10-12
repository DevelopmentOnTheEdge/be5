package com.developmentontheedge.be5.query;


public interface QuerySession
{
    Object get(String name);

    void set(String name, Object value);
}
