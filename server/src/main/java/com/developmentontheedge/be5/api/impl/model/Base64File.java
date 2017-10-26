package com.developmentontheedge.be5.api.impl.model;


public class Base64File
{
    private String name;
    private String data;

    public Base64File(String name, String data)
    {
        this.name = name;
        this.data = data;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getData()
    {
        return data;
    }

    public void setData(String data)
    {
        this.data = data;
    }

    @Override
    public String toString()
    {
        return "Base64File{" +
                "name='" + name + '\'' +
                '}';
    }
}
