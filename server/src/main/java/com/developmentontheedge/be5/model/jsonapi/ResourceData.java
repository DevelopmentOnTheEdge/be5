package com.developmentontheedge.be5.model.jsonapi;

public class ResourceData
{
    private String id;
    private String type;
    private Object attributes;
    private Object relationships;

    public ResourceData(String type, Object attributes)
    {
        this.type = type;
        this.attributes = attributes;
    }

    public String getId()
    {
        return id;
    }

    public String getType()
    {
        return type;
    }

    public Object getAttributes()
    {
        return attributes;
    }

    public Object getRelationships()
    {
        return relationships;
    }

    @Override
    public String toString()
    {
        return "ResourceData{" +
                (type!=null ? "type='" + type + '\'' : "") +
                (id!=null ? ", id='" + id + '\'' : "") +
                (attributes!=null ? ", attributes=" + attributes : "") +
                (relationships!=null ? ", relationships=" + relationships : "") +
        '}';
    }
}
