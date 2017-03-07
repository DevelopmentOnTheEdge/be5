package com.developmentontheedge.be5.api.operation;

/**
 * An option of a "select" element.
 * 
 * @author asko
 */
public class Option
{

    public final String value;
    public final String text;
    
    public Option(String value, String text)
    {
        this.text = text;
        this.value = value;
    }
    
}
