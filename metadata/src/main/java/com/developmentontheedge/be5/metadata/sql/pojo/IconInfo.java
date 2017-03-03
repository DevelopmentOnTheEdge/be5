package com.developmentontheedge.be5.metadata.sql.pojo;

public class IconInfo
{
    private String name;
    private String ownerId;
    private String mimeType;
    private byte[] data;
    private String origin;
    
    public String getName()
    {
        return name;
    }
    public void setName( String name )
    {
        this.name = name;
    }
    public String getOwnerId()
    {
        return ownerId;
    }
    public void setOwnerId( String ownerId )
    {
        this.ownerId = ownerId;
    }
    public String getMimeType()
    {
        return mimeType;
    }
    public void setMimeType( String mimeType )
    {
        this.mimeType = mimeType;
    }
    public byte[] getData()
    {
        return data;
    }
    public void setData( byte[] data )
    {
        this.data = data;
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
