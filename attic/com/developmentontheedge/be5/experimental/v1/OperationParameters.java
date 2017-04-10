package com.developmentontheedge.be5.api.experimental.v1;

import java.util.Optional;

import com.developmentontheedge.beans.DynamicPropertySet;

/**
 * Extract parameters of a operation from a dynamic property set.
 * 
 * @author asko
 */
public class OperationParameters
{
    
    private final DynamicPropertySet parameters;
    
    /**
     * Using of this constructor directly is acceptable.
     */
    public OperationParameters(DynamicPropertySet parameters)
    {
        this.parameters = parameters;
    }
    
    /**
     * Fails fast.
     */
    public String getString(String name)
    {
        Object value = parameters.getValue(name);
        if (value == null || value.toString().isEmpty())
            throw new IllegalArgumentException();
        return value.toString();
    }
    
    public String getString(String name, String defaultValue)
    {
        Object value = parameters.getValue(name);
        return value == null ? defaultValue : value.toString().trim();
    }
    
    /**
     * Fails fast.
     */
    public int getInt(String name)
    {
        return Integer.parseInt(parameters.getValue(name).toString());
    }
    
    public int getInt(String name, int defaultValue)
    {
        try
        {
            return Integer.parseInt(parameters.getValue(name).toString());
        }
        catch (Exception pokemon)
        {
            return defaultValue;
        }
    }
    
    public Optional<Integer> getOptionalInt(String name)
    {
        Object value = parameters.getValue(name);
        if (value.getClass() == Integer.class)
            return Optional.of((Integer) value);
        return Optional.empty();
    }
    
    /**
     * Fails fast.
     */
    public double getDouble(String name)
    {
        return Double.parseDouble(parameters.getValue(name).toString());
    }
    
}
