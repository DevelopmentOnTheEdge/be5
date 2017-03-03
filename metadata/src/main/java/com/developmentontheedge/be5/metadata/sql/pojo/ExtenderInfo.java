package com.developmentontheedge.be5.metadata.sql.pojo;

public class ExtenderInfo
{
    long operID;
    String role;
    String module_name;
    String class_name;
    String jsCode;
    int invokeOrder;
    public long getOperID()
    {
        return operID;
    }
    public void setOperID( long operID )
    {
        this.operID = operID;
    }
    public String getRole()
    {
        return role;
    }
    public void setRole( String role )
    {
        this.role = role;
    }
    public String getModule_name()
    {
        return module_name;
    }
    public void setModule_name( String module_name )
    {
        this.module_name = module_name;
    }
    public String getClass_name()
    {
        return class_name;
    }
    public void setClass_name( String class_name )
    {
        this.class_name = class_name;
    }
    public String getJsCode()
    {
        return jsCode;
    }
    public void setJsCode( String jsCode )
    {
        this.jsCode = jsCode;
    }
    public int getInvokeOrder()
    {
        return invokeOrder;
    }
    public void setInvokeOrder( int invokeOrder )
    {
        this.invokeOrder = invokeOrder;
    }
}
