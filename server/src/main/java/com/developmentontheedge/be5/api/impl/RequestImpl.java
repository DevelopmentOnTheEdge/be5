package com.developmentontheedge.be5.api.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.developmentontheedge.be5.api.Request;
import com.developmentontheedge.be5.api.exceptions.Be5Exception;
import com.developmentontheedge.be5.api.exceptions.impl.Be5ErrorCode;
import com.google.common.base.Strings;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class RequestImpl implements Request {
	
    private final HttpServletRequest rawRequest;
    private final String requestUri;
    private final Map<String, String> parameters;
    private final String remoteAddr;
    private final String sessionId;
    
    public RequestImpl(HttpServletRequest rawRequest, String requestUri, Map<String, String> parameters) {
        this.rawRequest = rawRequest;
        this.requestUri = requestUri;
        this.parameters = new HashMap<>(parameters);
        this.remoteAddr = rawRequest.getRemoteAddr();
        this.sessionId = rawRequest.getSession().getId();
    }
    
    @Override
    public Object getAttribute(String name) {
    	HttpSession session = rawRequest.getSession();
        return session == null ? null : session.getAttribute(name);
    }
    
    @Override
    public void setAttribute(String name, Object value) {
    	HttpSession session = rawRequest.getSession();
    	if(session != null)
    	    session.setAttribute(name, value);
    }
    
    @Override
    public Map<String, String> getValues(String parameter) throws Be5Exception {
		String valuesString = get(parameter);
		if(Strings.isNullOrEmpty(valuesString))
		{
			return Collections.emptyMap();
		}
        
        Map<String, String> fieldValues = new HashMap<>();
        
        try
        {
            JsonArray values = (JsonArray) new JsonParser().parse(valuesString);
            for (int i = 0; i < values.size(); i++)
            {
                JsonObject pair = (JsonObject) values.get(i);
                String name = pair.get("name").getAsString();
                String value = pair.get("value").getAsString();
                fieldValues.put(name, value);
            }
        }
        catch (ClassCastException e)
        {
            throw Be5ErrorCode.PARAMETER_INVALID.rethrow(e, parameter, valuesString);
        }
		return fieldValues;
    }

	@Override
    public Map<String, String> getParameters() {
        return Collections.unmodifiableMap(parameters);
    }
    
	@Override
    public String getRequestUri() {
        return requestUri;
    }
    
    @Override
    public String getRemoteAddr() {
        return remoteAddr;
    }

    @Override
    public String getSessionId() {
        return sessionId;
    }
    
	@Override
	public HttpServletRequest getRawRequest() {
		return rawRequest;
	}

    @Override
    public HttpSession getRawSession()
    {
        return getRawRequest().getSession();
    }
		
}
