package com.developmentontheedge.be5.test.mocks;

import com.developmentontheedge.be5.web.Request;
import com.developmentontheedge.be5.web.Session;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class ServerTestRequest implements Request
{
    private Session session = null;

    public ServerTestRequest(Session session)
    {
        this.session = session;
    }

    public ServerTestRequest()
    {
    }

    @Override
    public Session getSession()
    {
        return session == null ? new ServerTestSession() : session;
    }

    @Override
    public Session getSession(boolean create)
    {
        if(create)return getSession();
        else return session;
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
    public String getServerUrl()
    {
        return null;
    }

    @Override
    public String getServerUrlWithContext()
    {
        return null;
    }

    @Override
    public String getContextPath()
    {
        return null;
    }

    @Override
    public String getBody()
    {
        return null;
    }

    @Override
    public Locale getLocale()
    {
        return null;
    }

    @Override
    public Map<String, String[]> getParameters()
    {
        return null;
    }

    @Override
    public String get(String parameter)
    {
        return null;
    }

    @Override
    public List<String> getList(String parameter)
    {
        return null;
    }

    @Override
    public String[] getParameterValues(String name)
    {
        return new String[0];
    }

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
}
