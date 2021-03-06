package com.developmentontheedge.be5.test.mocks;

import com.developmentontheedge.be5.web.Request;
import com.developmentontheedge.be5.web.Session;

import javax.inject.Inject;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class ServerTestRequest implements Request
{
    private Map<String, Cookie> cookies = new HashMap<>();
    private Session session = null;

    private Map<String, String[]> parameters = new HashMap<>();

    @Inject
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
        return session;
    }

    @Override
    public Session getSession(boolean create)
    {
        return session;
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
    public ServletInputStream getInputStream()
    {
        return null;
    }

    @Override
    public Map<String, String[]> getParameters()
    {
        return parameters;
    }

    @Override
    public String get(String parameter)
    {
        String[] values = parameters.get(parameter);
        return values != null ? values[0] : null;
    }

    public void setParameter(String name, String value)
    {
        parameters.put(name, new String[]{value});
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
        return session.get(name);
    }

    @Override
    public void setAttribute(String name, Object value)
    {
        session.set(name, value);
    }

    @Override
    public Cookie[] getCookies()
    {
        return cookies.values().toArray(new Cookie[0]);
    }

    public void setCookies(Cookie... cookies)
    {
        Arrays.stream(cookies).forEach(c -> this.cookies.put(c.getName(), c));
    }
}
