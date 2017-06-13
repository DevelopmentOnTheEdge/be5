package com.developmentontheedge.be5.api.experimental;

import java.util.Map;

import com.developmentontheedge.be5.api.Request;
import com.developmentontheedge.be5.components.RestApiConstants;

/**
 * Extracts parameters of an operation from a request.
 * 
 * @author asko
 */
public class OperationRequest
{
    
    private final Request req;
    
    /**
     * Using of this constructor directly is acceptable.
     */
    public OperationRequest(Request req)
    {
        this.req = req;
    }
    
    /**
     * Nullable
     */
    public String get(String parameterName)
    {
        return req.getValues(RestApiConstants.VALUES).get(parameterName);
    }
    
    /**
     * Returns an unordered map of values.
     */
    public Map<String, String> getAll()
    {
        return req.getValues(RestApiConstants.VALUES);
    }
    
}
