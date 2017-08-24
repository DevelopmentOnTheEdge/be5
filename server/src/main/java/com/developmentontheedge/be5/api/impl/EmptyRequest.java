package com.developmentontheedge.be5.api.impl;

import com.developmentontheedge.be5.api.Request;
import com.developmentontheedge.be5.api.exceptions.Be5Exception;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

public class EmptyRequest implements Request
{
    @Override
    public Map<String, Object> getValues(String parameterName) throws Be5Exception
    {
        return new HashMap<>();
    }

    @Override
    public Map<String, String> getStringValues(String parameterName) throws Be5Exception
    {
        return new HashMap<>();
    }

    @Override
    public String getRequestUri()
    {
        return "";
    }

    @Override
    public String getRemoteAddr()
    {
        return "";
    }

    @Override
    public HttpServletRequest getRawRequest()
    {
        return null;
    }

    @Override
    public HttpSession getRawSession()
    {
        return null;
    }

    @Override
    public String getServletContextRealPath(String s)
    {
        return "";
    }

    @Override
    public String getBaseUrl()
    {
        return "";
    }

    @Override
    public Map<String, String> getParameters()
    {
        return new HashMap<>();
    }

    @Override
    public String getSessionId()
    {
        return "";
    }

    @Override
    public Object getAttribute(String name)
    {
        return null;
    }

    @Override
    public void setAttribute(String name, Object value)
    {

    }
}
