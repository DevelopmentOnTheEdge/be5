package com.developmentontheedge.be5.web;

import java.util.List;
import java.util.Map;

import com.developmentontheedge.be5.base.exceptions.Be5Exception;

/**
 * <p>An interface providing access to key-value style parameters (e.g. from HTTP request or WebSocket request).
 * <code>Controller</code>'s {@link Request} implement this interface.</p>
 * 
 * <p>Parameters of HTTP requests are get parameters or fields of the <code>x-www-form-urlencoded</code> content.</p>
 * 
 * @author lan
 * @see Request
 */
public interface ParametersAccess
{
    /**
     * Returns an unchangeable map of request parameters.
     */
    Map<String, String[]> getParameters();

    String get(String parameter);

    List<String> getList(String parameter);

    String[] getParameterValues(String name);
    
    default int getInt(String parameter) throws Be5Exception
    {
        String s = get(parameter);
        
        if (s == null)
            throw Be5Exception.requestParameterIsAbsent(parameter);
        
        try
        {
            return Integer.parseInt(s);
        }
        catch (NumberFormatException e)
        {
            throw Be5Exception.invalidRequestParameter(parameter, s);
        }
    }

    default int getInt(String parameter, int defaultValue) throws Be5Exception
    {
        String s = get(parameter);
        
        if (s == null)
            return defaultValue;
        
        try
        {
            return Integer.parseInt(s);
        }
        catch (NumberFormatException e)
        {
            return defaultValue;
        }
    }
    
    /**
     * Returns a request parameter or empty string if there's no such parameter.
     * 
     * @see ParametersAccess#get(String)
     */
    default String getOrEmpty(String parameter)
    {
        String value = get( parameter );
        return value == null ? "" : value;
    }

    default String getOrDefault(String parameter, String defaultValue)
    {
        String value = get( parameter );
        return value == null ? defaultValue : value;
    }

    /**
     * Returns a boolean request parameter or the given default value if there's no such parameter.
     * @throws Be5Exception if the parameter is present, but it isn't a boolean value
     * 
     * @see ParametersAccess#get(String)
     */
    default boolean getBoolean(String parameter, boolean defaultValue) throws Be5Exception
    {
        String value = get( parameter );
        if( value != null && !value.equals( "true" ) && !value.equals( "false" ) )
            throw Be5Exception.requestParameterIsAbsent(parameter);
        if( value != null )
            return Boolean.parseBoolean( value );
        return defaultValue;
    }

    /**
     * Returns a request parameter.
     * @throws Be5Exception if parameter is absent or empty
     * 
     * @see ParametersAccess#get(String)
     */
    default String getNonEmpty(String parameter) throws Be5Exception
    {
        String value = get( parameter );
        if( value == null )
            throw Be5Exception.requestParameterIsAbsent(parameter);
        value = value.trim();
        if( value.isEmpty() )
            throw Be5Exception.requestParameterIsAbsent(parameter);
        return value;
    }
}
