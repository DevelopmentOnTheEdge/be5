/* Generated By:JJTree: Do not edit this line. AstConstant.java */
package com.developmentontheedge.sql.model;

import java.util.Objects;

public class AstConstant extends SimpleNode
{
    public AstConstant(int id)
    {
        super(id);
    }

    private Object value;
    public Object getValue()
    {
        return value;
    }
    public void setValue(Object val)
    {
        value = val;
        updateContent();
    }
    private void updateContent()
    {
        nodeContent = name == null ? value == null ? null : value.toString() : name;
    }

    // name of predefined constant:
    private String name;
    public String getName()
    {
        return name;
    }
    /** 
     * Sets the name of the predefined constant.
     * @param name new name 
     */
    public void setName(String name)
    {
        this.name = name;
        updateContent();
    }

    @Override
    public String toString()
    {
        return "Constant: " + ( name != null ? name + "=" : "" ) + value;
    }

    @Override
    public boolean equals(Object obj)
    {
        if( this == obj )
            return true;
        if( obj == null || getClass() != obj.getClass() )
            return false;
        final AstConstant other = (AstConstant)obj;
        return Objects.equals( name, other.name ) && Objects.equals( value, other.value );
    }
}
