package com.developmentontheedge.be5.model;


public class FrontendAction
{
    private final String type;
    private final Object value;

    public FrontendAction(String type, Object value)
    {
        this.type = type;
        this.value = value;
    }

    public String getType()
    {
        return type;
    }

    public Object getValue()
    {
        return value;
    }
}
