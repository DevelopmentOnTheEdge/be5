package com.developmentontheedge.be5.test.mocks;

import com.developmentontheedge.be5.web.Request;
import com.developmentontheedge.be5.web.Session;

import javax.inject.Inject;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.mockito.Mockito.mock;


public class TestRequest implements Request
{
    private Session session = null;

    public static Request mock = mock(Request.class);

    public static void newMock()
    {
        mock = mock(Request.class);
    }

    @Inject
    public TestRequest(Session session)
    {
        this.session = session;
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
        return mock.getRequestUri();
    }

    @Override
    public String getRemoteAddr()
    {
        return mock.getRemoteAddr();
    }

    @Override
    public HttpServletRequest getRawRequest()
    {
        return mock.getRawRequest();
    }

    @Override
    public HttpSession getRawSession()
    {
        return mock.getRawSession();
    }

    @Override
    public String getServerUrl()
    {
        return mock.getServerUrl();
    }

    @Override
    public String getServerUrlWithContext()
    {
        return mock.getServerUrlWithContext();
    }

    @Override
    public String getContextPath()
    {
        return mock.getContextPath();
    }

    @Override
    public String getBody()
    {
        return mock.getBody();
    }

    @Override
    public Locale getLocale()
    {
        return mock.getLocale();
    }

    @Override
    public ServletInputStream getInputStream() throws IOException
    {
        return mock.getInputStream();
    }

    @Override
    public Map<String, String[]> getParameters()
    {
        return mock.getParameters();
    }

    @Override
    public String get(String parameter)
    {
        return mock.get(parameter);
    }

    @Override
    public List<String> getList(String parameter)
    {
        return mock.getList(parameter);
    }

    @Override
    public String[] getParameterValues(String name)
    {
        return mock.getParameterValues(name);
    }

    @Override
    public String getSessionId()
    {
        return session.getSessionId();
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
        return new Cookie[0];
    }
}
