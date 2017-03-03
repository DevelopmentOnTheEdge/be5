package com.developmentontheedge.be5.metadata.sql.pojo;

public class StaticPageInfo
{
    private String lang, name, content;
    private long id;
    
    public String getLang()
    {
        return lang;
    }
    public void setLang( String lang )
    {
        this.lang = lang;
    }
    public String getName()
    {
        return name;
    }
    public void setName( String name )
    {
        this.name = name;
    }
    public String getContent()
    {
        return content;
    }
    public void setContent( String content )
    {
        this.content = content;
    }
    public long getId()
    {
        return id;
    }
    public void setId( long id )
    {
        this.id = id;
    }
}
