package com.developmentontheedge.be5.query.impl.beautifiers;

import com.google.common.base.Preconditions;

import java.util.HashMap;
import java.util.Map;

public class BeautifierCollection
{
    private final Map<String, SubQueryBeautifier> subQueryBeautifiers = new HashMap<>();

    public void addBeautifier(String name, SubQueryBeautifier subQueryBeautifier)
    {
        Preconditions.checkArgument(!subQueryBeautifiers.containsKey(name));
        subQueryBeautifiers.put(name, subQueryBeautifier);
    }

    public SubQueryBeautifier get(String name)
    {
        return subQueryBeautifiers.get(name);
    }
}
