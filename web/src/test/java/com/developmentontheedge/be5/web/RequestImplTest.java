package com.developmentontheedge.be5.web;

import com.developmentontheedge.be5.web.impl.RequestImpl;
import com.developmentontheedge.be5.web.impl.SessionImpl;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class RequestImplTest
{
    private HttpServletRequest rawRequest;
    private Request req;

    @Before
    public void setUp()
    {
        rawRequest = mock(HttpServletRequest.class);
        when(rawRequest.getSession()).thenReturn(mock(HttpSession.class));
        req = new RequestImpl(rawRequest);
    }

    @Test
    public void getParameterValues()
    {
        when(rawRequest.getParameterValues("ids[]")).thenReturn(new String[]{"1", "2"});

        assertEquals(Arrays.asList("1", "2"), req.getList("ids"));
    }

    @Test
    public void getParameterValuesOneValue()
    {
        when(rawRequest.getParameter("ids")).thenReturn("1");

        assertEquals(Collections.singletonList("1"), req.getList("ids"));
    }

    @Test
    public void getSession_false()
    {
        when(rawRequest.getSession(true)).thenReturn(mock(HttpSession.class));
        assertEquals(null, req.getSession(false));
        verify(rawRequest).getSession(false);
    }

    @Test
    public void getSession_true()
    {
        when(rawRequest.getSession(true)).thenReturn(mock(HttpSession.class));
        assertEquals(SessionImpl.class, req.getSession(true).getClass());
        verify(rawRequest).getSession(true);
    }

    @Test
    public void getSession()
    {
        when(rawRequest.getSession()).thenReturn(mock(HttpSession.class));
        assertEquals(SessionImpl.class, req.getSession().getClass());
        verify(rawRequest).getSession();
    }

    @Test
    public void getBody() throws IOException
    {
        Reader inputString = new StringReader("test body\n2");
        BufferedReader bufferedReader = new BufferedReader(inputString);
        when(rawRequest.getReader()).thenReturn(bufferedReader);

        assertEquals("test body\n2", req.getBody());
    }

    @Test
    public void getUrl()
    {
        when(rawRequest.getScheme()).thenReturn("http");
        when(rawRequest.getServerName()).thenReturn("localhost");
        when(rawRequest.getServerPort()).thenReturn(80);
        when(rawRequest.getContextPath()).thenReturn("");

        assertEquals("http://localhost", req.getServerUrl());
    }

    @Test
    public void getUrl8080()
    {
        when(rawRequest.getScheme()).thenReturn("http");
        when(rawRequest.getServerName()).thenReturn("localhost");
        when(rawRequest.getServerPort()).thenReturn(8080);
        when(rawRequest.getContextPath()).thenReturn("");

        assertEquals("http://localhost:8080", req.getServerUrl());
    }

    @Test
    public void getServerUrlWithContext()
    {
        when(rawRequest.getScheme()).thenReturn("http");
        when(rawRequest.getServerName()).thenReturn("localhost");
        when(rawRequest.getServerPort()).thenReturn(80);
        when(rawRequest.getContextPath()).thenReturn("");

        assertEquals("http://localhost", req.getServerUrlWithContext());
    }

    @Test
    public void getServerUrlWithContextPath()
    {
        when(rawRequest.getScheme()).thenReturn("http");
        when(rawRequest.getServerName()).thenReturn("localhost");
        when(rawRequest.getServerPort()).thenReturn(80);
        when(rawRequest.getContextPath()).thenReturn("/path");

        assertEquals("http://localhost/path", req.getServerUrlWithContext());
    }

    @Test
    public void getContextPath()
    {
        when(rawRequest.getContextPath()).thenReturn("/path");

        assertEquals("/path", req.getContextPath());
    }
}