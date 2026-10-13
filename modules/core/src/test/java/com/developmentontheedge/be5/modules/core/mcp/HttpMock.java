package com.developmentontheedge.be5.modules.core.mcp;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * A servlet request and the response to it, for calling servlets directly.
 */
public class HttpMock
{
    public final HttpServletRequest request = mock(HttpServletRequest.class);
    public final HttpServletResponse response = mock(HttpServletResponse.class);

    public int status = 200;
    public final Map<String, String> headers = new HashMap<>();

    private final Map<String, String> params = new HashMap<>();
    private final Map<String, String> requestHeaders = new HashMap<>();
    private final StringWriter body = new StringWriter();

    public HttpMock(String method, String uri) throws Exception
    {
        requestHeaders.put("Host", "be5.example.org");
        when(request.getMethod()).thenReturn(method);
        when(request.getRequestURI()).thenReturn(uri);
        when(request.getContextPath()).thenReturn("");
        when(request.getScheme()).thenReturn("http");
        when(request.getRemoteAddr()).thenReturn("192.0.2.1");
        when(request.getParameter(anyString())).thenAnswer(inv -> params.get((String) inv.getArguments()[0]));
        when(request.getHeader(anyString())).thenAnswer(inv -> requestHeaders.get((String) inv.getArguments()[0]));

        when(response.getWriter()).thenReturn(new PrintWriter(body));
        doAnswer(inv ->
        {
            status = (Integer) inv.getArguments()[0];
            return null;
        }).when(response).setStatus(anyInt());
        doAnswer(inv -> headers.put((String) inv.getArguments()[0], (String) inv.getArguments()[1]))
                .when(response).setHeader(anyString(), anyString());
    }

    public HttpMock param(String name, String value)
    {
        params.put(name, value);
        return this;
    }

    public HttpMock params(Map<String, String> values)
    {
        params.putAll(values);
        return this;
    }

    public HttpMock header(String name, String value)
    {
        requestHeaders.put(name, value);
        return this;
    }

    public HttpMock body(String json) throws Exception
    {
        when(request.getReader()).thenReturn(new BufferedReader(new StringReader(json)));
        return this;
    }

    public HttpMock session(String id, String attribute, Object value)
    {
        HttpSession session = mock(HttpSession.class);
        when(session.getId()).thenReturn(id);
        when(session.getAttribute(attribute)).thenReturn(value);
        when(request.getSession(false)).thenReturn(session);
        return this;
    }

    public String body()
    {
        return body.toString();
    }
}
