package com.developmentontheedge.be5.metadata.sql.pojo;

public class QueryPerRoleInfo
{
    private String role;
    private boolean isDefault;
    
    public String getRole()
    {
        return role;
    }
    public void setRole( String role )
    {
        this.role = role;
    }
    public boolean isDefault()
    {
        return isDefault;
    }
    public void setDefault( boolean isDefault )
    {
        this.isDefault = isDefault;
    }
}
