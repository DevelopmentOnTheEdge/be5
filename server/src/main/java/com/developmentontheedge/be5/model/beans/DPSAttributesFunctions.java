package com.developmentontheedge.be5.model.beans;

import java.util.HashMap;
import java.util.Map;

public class DPSAttributesFunctions
{
    public void name(String value)
    {
        this.name = value;
    }

    public void type(Class<?> value)
    {
        this.TYPE = value;
    }

    public void displayName(String value)
    {
        map.put("DISPLAY_NAME", value);
    }

    public void multipleSelectionList(boolean value)
    {
        map.put("MULTIPLE_SELECTION_LIST", value);
    }

    public void tagList(Object value)
    {
        map.put("TAG_LIST_ATTR", value);
    }

    public void value(Object value)
    {
        map.put("value", value);
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

    private Map<String, Object> map = new HashMap<String, Object>();
    private String name;
    private Class<?> TYPE = String.class;
}
