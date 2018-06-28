package com.developmentontheedge.be5.test.mocks;

import com.developmentontheedge.be5.web.Response;

import javax.servlet.http.HttpServletResponse;

import static org.mockito.Mockito.mock;


public class TestResponse implements Response
{
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
    public void sendErrorAsJson(Object value, int status)
    {
        mock.sendErrorAsJson(value, status);
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
    public HttpServletResponse getRawResponse()
    {
        return mock.getRawResponse();
    }

    @Override
    public void redirect(String location)
    {
        mock.redirect(location);
    }
}
