package com.developmentontheedge.be5.metadata.sql.pojo;

public class DaemonInfo
{
    private long ID;
    private String name;
    private String className;
    private String configSection;
    private String daemonType;
    private String description;
    private int slaveNo;
    private String origin;
    
    public long getID()
    {
        return ID;
    }
    public void setID( long iD )
    {
        ID = iD;
    }
    public String getName()
    {
        return name;
    }
    public void setName( String name )
    {
        this.name = name;
    }
    public String getClassName()
    {
        return className;
    }
    public void setClassName( String className )
    {
        this.className = className;
    }
    public String getConfigSection()
    {
        return configSection;
    }
    public void setConfigSection( String configSection )
    {
        this.configSection = configSection;
    }
    public String getDaemonType()
    {
        return daemonType;
    }
    public void setDaemonType( String daemonType )
    {
        this.daemonType = daemonType;
    }
    public int getSlaveNo()
    {
        return slaveNo;
    }
    public void setSlaveNo( int slaveNo )
    {
        this.slaveNo = slaveNo;
    }
    public String getOrigin()
    {
        return origin;
    }
    public void setOrigin( String origin )
    {
        this.origin = origin;
    }
    public String getDescription()
    {
        return description;
    }
    public void setDescription( String description )
    {
        this.description = description;
    }
}
