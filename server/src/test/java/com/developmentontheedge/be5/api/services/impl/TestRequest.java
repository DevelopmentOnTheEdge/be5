package com.developmentontheedge.be5.api.services.impl;

import com.developmentontheedge.be5.api.Request;
import com.developmentontheedge.be5.api.exceptions.Be5Exception;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Map;

class TestRequest implements Request
{

    @Override
    public String getSessionId()
    {
        return null;
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

    @Override
    public Map<String, String> getValues(String parameterName) throws Be5Exception
    {
        return null;
    }

    @Override
    public String getRequestUri()
    {
        return null;
    }

    @Override
    public String getRemoteAddr()
    {
        return null;
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
    public Map<String, String> getParameters()
    {
        return null;
    }
}
