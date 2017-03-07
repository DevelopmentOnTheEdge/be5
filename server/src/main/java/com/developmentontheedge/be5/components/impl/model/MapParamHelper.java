package com.developmentontheedge.be5.components.impl.model;

import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.Map;

import com.developmentontheedge.be5.ParamHelper;
import com.google.common.base.Strings;

public class MapParamHelper implements ParamHelper
{
    
    private final Map<String, String> map;

    public MapParamHelper(Map<String, String> map)
    {
        this.map = new LinkedHashMap<>(map);
    }
    
    @Override
    public void remove(String name)
    {
        map.remove(name);
    }

    @Override
    public String get(String name)
    {
        return map.get(name);
    }

    /**
     * Returns null if the parameter is empty.
     */
    @Override
    public String getStrict(String name)
    {
        return Strings.emptyToNull(map.get(name));
    }

    @Override
    public String[] getValues(String name)
    {
        if (map.containsKey(name))
        {
            return new String[0];
        }
        
        return new String[] { map.get(name) };
    }
    
    @Override
    public void put(String name, String value) throws Exception
    {
        map.put(name, value);
    }

    @Override
    public void put(String name, String[] value) throws Exception
    {
        map.put(name, value[0]);
    }

    @Override
    public Hashtable<String, String> getCompleteParamTable()
    {
        return new Hashtable<>(map);
    }
    
    @Override
    public Hashtable<String, String> getNonStandardTable()
    {
        return new Hashtable<>(map);
    }
    
}
