package com.developmentontheedge.be5.metadata.sql.pojo;

public class QuerySettingsInfo
{
    private long queryID;
    private String role_name;
    private int maxRecordsPerPage;
    private int maxRecordsPerPrintPage;
    private int maxRecordsInDynamicDropDown;
    private Long colorSchemeID;
    private int autoRefresh;
    private String beautifier;
    
    public long getQueryID()
    {
        return queryID;
    }
    public void setQueryID( long queryID )
    {
        this.queryID = queryID;
    }
    public String getRole_name()
    {
        return role_name;
    }
    public void setRole_name( String role_name )
    {
        this.role_name = role_name;
    }
    public int getMaxRecordsPerPage()
    {
        return maxRecordsPerPage;
    }
    public void setMaxRecordsPerPage( int maxRecordsPerPage )
    {
        this.maxRecordsPerPage = maxRecordsPerPage;
    }
    public int getMaxRecordsPerPrintPage()
    {
        return maxRecordsPerPrintPage;
    }
    public void setMaxRecordsPerPrintPage( int maxRecordsPerPrintPage )
    {
        this.maxRecordsPerPrintPage = maxRecordsPerPrintPage;
    }
    public int getMaxRecordsInDynamicDropDown()
    {
        return maxRecordsInDynamicDropDown;
    }
    public void setMaxRecordsInDynamicDropDown( int maxRecordsInDynamicDropDown )
    {
        this.maxRecordsInDynamicDropDown = maxRecordsInDynamicDropDown;
    }
    public Long getColorSchemeID()
    {
        return colorSchemeID;
    }
    public void setColorSchemeID( Long colorSchemeID )
    {
        this.colorSchemeID = colorSchemeID;
    }
    public int getAutoRefresh()
    {
        return autoRefresh;
    }
    public void setAutoRefresh( int autoRefresh )
    {
        this.autoRefresh = autoRefresh;
    }
    public String getBeautifier()
    {
        return beautifier;
    }
    public void setBeautifier( String beautifier )
    {
        this.beautifier = beautifier;
    }
}
