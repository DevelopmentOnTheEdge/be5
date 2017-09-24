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

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ResourceData that = (ResourceData) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (type != null ? !type.equals(that.type) : that.type != null) return false;
        if (attributes != null ? !attributes.equals(that.attributes) : that.attributes != null) return false;
        return relationships != null ? relationships.equals(that.relationships) : that.relationships == null;
    }

    @Override
    public int hashCode()
    {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (attributes != null ? attributes.hashCode() : 0);
        result = 31 * result + (relationships != null ? relationships.hashCode() : 0);
        return result;
    }
}
