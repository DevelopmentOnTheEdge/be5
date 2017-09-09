package com.developmentontheedge.be5.model.beans;

import java.util.HashMap;
import java.util.Map;

public class DPSAttributes
{
    private Map<String, Object> map = new HashMap<>();

    private String name;
    private Class<?> TYPE = String.class;

    public void setName(String value)
    {
        name = value;
    }

    public void setDISPLAY_NAME(String value)
    {
        map.put("DISPLAY_NAME", value);
    }
    public void setTYPE(Class<?> value)
    {
        TYPE = value;
    }
    public void setValue(Object value)
    {
        map.put("value", value);
    }

    public void setTAG_LIST_ATTR(Object value)
    {
        map.put("TAG_LIST_ATTR", value);
    }
    public void setREAD_ONLY(boolean value)
    {
        map.put("READ_ONLY", value);
    }

    public void setRELOAD_ON_CHANGE(boolean value)
    {
        map.put("RELOAD_ON_CHANGE", value);
    }

    public void setGROUP_NAME(Object value)
    {
        map.put("GROUP_NAME", value);
    }

    public void setGROUP_ID(Object value)
    {
        map.put("GROUP_ID", value);
    }

    public void setMULTIPLE_SELECTION_LIST(boolean value)
    {
        map.put("MULTIPLE_SELECTION_LIST", value);
    }

    public Map<String, Object> getMap()
    {
        return map;
    }

    public String getName()
    {
        return name;
    }

    public Class<?> getTYPE()
    {
        return TYPE;
    }
}
