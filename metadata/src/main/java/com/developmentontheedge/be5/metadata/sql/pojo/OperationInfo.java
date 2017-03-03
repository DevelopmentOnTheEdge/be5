package com.developmentontheedge.be5.metadata.sql.pojo;

public class OperationInfo
{
    private long ID;
    private String name;
    private String type;
    private String notSupported;
    private String code;
    private String ___hashCode;
    private Integer requiredRecordSetSize;
    private Integer executionPriority;
    private String logging;
    private String isSecure;
    private String isConfirm;
    private Long contextID;
    private Long categoryID;
    private String wellKnownName;
    private String table_name;
    private String origin;
    
    public String getTable_name()
    {
        return table_name;
    }
    
    public void setTable_name( String table_name )
    {
        this.table_name = table_name;
    }
    
    public long getID()
    {
        return ID;
    }
    public void setID(long ID)
    {
        this.ID = ID;
    }
    public String getName()
    {
        return name;
    }
    public void setName(String name)
    {
        this.name = name;
    }
    public String getType()
    {
        return type;
    }
    public void setType(String type)
    {
        this.type = type;
    }
    public String getNotSupported()
    {
        return notSupported;
    }
    public void setNotSupported(String notSupported)
    {
        this.notSupported = notSupported;
    }
    public String getCode()
    {
        return code;
    }
    public void setCode(String code)
    {
        this.code = code;
    }
    public String get___hashCode()
    {
        return ___hashCode;
    }
    public void set___hashCode(String ___hashCode)
    {
        this.___hashCode = ___hashCode;
    }
    public Integer getRequiredRecordSetSize()
    {
        return requiredRecordSetSize;
    }
    public void setRequiredRecordSetSize(Integer requiredRecordSetSize)
    {
        this.requiredRecordSetSize = requiredRecordSetSize;
    }
    public Integer getExecutionPriority()
    {
        return executionPriority;
    }
    public void setExecutionPriority(Integer executionPriority)
    {
        this.executionPriority = executionPriority;
    }
    public String getLogging()
    {
        return logging;
    }
    public void setLogging(String logging)
    {
        this.logging = logging;
    }
    public String getIsSecure()
    {
        return isSecure;
    }
    public void setIsSecure(String isSecure)
    {
        this.isSecure = isSecure;
    }
    public String getIsConfirm()
    {
        return isConfirm;
    }
    public void setIsConfirm(String isConfirm)
    {
        this.isConfirm = isConfirm;
    }
    public Long getContextID()
    {
        return contextID;
    }
    public void setContextID(Long contextID)
    {
        this.contextID = contextID;
    }
    public Long getCategoryID()
    {
        return categoryID;
    }
    public void setCategoryID(Long categoryID)
    {
        this.categoryID = categoryID;
    }
    public String getWellKnownName()
    {
        return wellKnownName;
    }
    public void setWellKnownName(String wellKnownName)
    {
        this.wellKnownName = wellKnownName;
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