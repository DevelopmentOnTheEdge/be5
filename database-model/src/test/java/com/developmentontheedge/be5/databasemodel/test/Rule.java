package com.developmentontheedge.be5.databasemodel.test;

public class Rule
{
    private String type;
    private Object attr;

    public Rule(String type, Object attr)
    {
        this.type = type;
        this.attr = attr;
    }
    public String getType()
    {
        return type;
    }

    public Object getAttr()
    {
        return attr;
    }

}
