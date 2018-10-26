package com.developmentontheedge.be5.query;


@FunctionalInterface
public interface VarResolver
{
    Object resolve(String varName);
}
