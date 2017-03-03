package com.developmentontheedge.be5.metadata.sql.pojo;

public class EntityInfo
{
    private String name;
    private String displayName;
    private String primaryKeyColumn;
    private String type;
    private String origin;
    
    public String getName()
    {
        return name;
    }
    public void setName(String name)
    {
        this.name = name;
    }
    public String getDisplayName()
    {
        return displayName;
    }
    public void setDisplayName(String displayName)
    {
        this.displayName = displayName;
    }
    public String getPrimaryKeyColumn()
    {
        return primaryKeyColumn;
    }
    public void setPrimaryKeyColumn(String primaryKeyColumn)
    {
        this.primaryKeyColumn = primaryKeyColumn;
    }
    public String getType()
    {
        return type;
    }
    public void setType(String type)
    {
        this.type = type;
    }
    public String getOrigin()
    {
        return origin;
    }
    public void setOrigin(String origin)
    {
        this.origin = origin;
    }
}