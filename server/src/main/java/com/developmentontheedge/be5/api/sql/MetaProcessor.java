package com.developmentontheedge.be5.api.sql;

import java.util.Map;

@FunctionalInterface
public interface MetaProcessor
{
    void process(Object value, Map<String, Map<String, String>> meta);
}
