package com.developmentontheedge.be5.test;

import com.developmentontheedge.be5.web.Response;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.mock;


public class ServerTestResponse implements Response
{
    private Map<String, Cookie> cookies = new HashMap<>();
    public static Response mock = mock(Response.class);

    public static void newMock()
    {
        mock = mock(Response.class);
    }

    @Override
    public void sendAsJson(Object value)
    {
        mock.sendAsJson(value);
    }

    @Override
    public void sendAsJson(Object value, int status)
    {
        mock.sendAsJson(value, status);
    }

    @Override
    public void setStatus(int status)
    {
        mock.setStatus(status);
    }

    @Override
    public void sendJson(String json)
    {
        mock.sendJson(json);
    }

    @Override
    public void sendHtml(String json)
    {
        mock.sendHtml(json);
    }

    @Override
    public void sendXml(String xml)
    {
        mock.sendXml(xml);
    }

    @Override
    public void sendYaml(String yaml)
    {
        mock.sendYaml(yaml);
    }

    @Override
    public HttpServletResponse getRawResponse()
    {
        return mock.getRawResponse();
    }

    @Override
    public void redirect(String location)
    {
        mock.redirect(location);
    }

    @Override
    public OutputStream getOutputStream() throws IOException
    {
        return mock.getOutputStream();
    }

    @Override
    public void addCookie(Cookie cookie)
    {
        cookies.put(cookie.getName(), cookie);
    }

    public Cookie getCookie(String name)
    {
        return cookies.get(name);
    }

    public void clearCookies()
    {
        cookies.clear();
    }
}
