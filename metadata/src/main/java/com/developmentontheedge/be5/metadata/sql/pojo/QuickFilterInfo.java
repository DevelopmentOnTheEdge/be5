package com.developmentontheedge.be5.metadata.sql.pojo;

public class QuickFilterInfo
{
    private long queryID;
    private String filter_param;
    private String name;
    private String filterQueryName;
    private String filteringClass;
    private String origin;
    
    public long getQueryID()
    {
        return queryID;
    }
    public void setQueryID( long queryID )
    {
        this.queryID = queryID;
    }
    public String getFilter_param()
    {
        return filter_param;
    }
    public void setFilter_param( String filter_param )
    {
        this.filter_param = filter_param;
    }
    public String getName()
    {
        return name;
    }
    public void setName( String name )
    {
        this.name = name;
    }
    public String getFilteringClass()
    {
        return filteringClass;
    }
    public void setFilteringClass( String filteringClass )
    {
        this.filteringClass = filteringClass;
    }
    public String getFilterQueryName()
    {
        return filterQueryName;
    }
    public void setFilterQueryName( String filterQueryName )
    {
        this.filterQueryName = filterQueryName;
    }
    public String getOrigin()
    {
        return origin;
    }
    public void setOrigin( String origin )
    {
        this.origin = origin;
    }
}
