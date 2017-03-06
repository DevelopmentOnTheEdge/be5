package com.developmentontheedge.be5.api;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.developmentontheedge.be5.api.exceptions.Be5Exception;

/**
 * Request injected to components.
 * 
 * @see Component
 * @author asko
 */
public interface Request extends SessionAccess, ParametersAccess {
    
    /**
     * Reads a parameter as JSON array in the form of [{name: 'foo', value: 'bar'},...]
     * 
     * @param parameterName parameter name
     * @return read map
     * @throws Be5Exception if parameter has invalid format
     */
    Map<String, String> getValues(String parameterName) throws Be5Exception;
    
	/**
     * Returns a remaining part of the request URI after the component ID.
     */
    String getRequestUri();
    
    /**
     * Returns the IP address of the client or last proxy that sent the request.
     */
    String getRemoteAddr();
    
	/**
     * Low-level request.
     */
    HttpServletRequest getRawRequest();

    /**
     * Low-level session.
     */
    HttpSession getRawSession();

}
