package com.developmentontheedge.be5.api;

import java.util.Map;

import com.developmentontheedge.be5.api.exceptions.Be5Exception;
import com.developmentontheedge.be5.api.exceptions.impl.Be5ErrorCode;

/**
 * <p>An interface providing access to key-value style parameters (e.g. from HTTP request or WebSocket request).
 * <code>Component</code>'s {@link Request} and <code>WebSocketComponent</code>'s {@link WebSocketContext} implement this interface.</p>
 * 
 * <p>Parameters of HTTP requests are get parameters or fields of the <code>x-www-form-urlencoded</code> content.</p>
 * 
 * @author lan
 * @see Request
 * @see WebSocketContext
 */
public interface ParametersAccess
{
    /**
     * Returns a request parameter or null if there's no such parameter.
     * 
     * @see ParametersAccess#getParameters()
     */
    default String get(String parameter)
    {
        return getParameters().get( parameter );
    }
    
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
        String value = getParameters().get( parameter );
        return value == null ? "" : value;
    }

    /**
     * Returns a boolean request parameter or the given default value if there's no such parameter.
     * @throws Be5Exception if the parameter is present, but it isn't a boolean value
     * 
     * @see ParametersAccess#get(String)
     */
    default boolean get(String parameter, boolean defaultValue) throws Be5Exception
    {
        String value = getParameters().get( parameter );
        if( value != null && !value.equals( "true" ) && !value.equals( "false" ) )
            throw Be5ErrorCode.PARAMETER_ABSENT.exception( parameter );
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
        String value = getParameters().get( parameter );
        if( value == null )
            throw Be5ErrorCode.PARAMETER_ABSENT.exception( parameter );
        value = value.trim();
        if( value.isEmpty() )
            throw Be5ErrorCode.PARAMETER_EMPTY.exception( parameter );
        return value;
    }

    /**
     * Returns an unchangeable map of request parameters.
     */
    Map<String, String> getParameters();
}
