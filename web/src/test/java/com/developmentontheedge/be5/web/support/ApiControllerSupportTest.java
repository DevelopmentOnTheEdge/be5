package com.developmentontheedge.be5.web.support;

import com.developmentontheedge.be5.web.Request;
import com.developmentontheedge.be5.web.Response;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class ApiControllerSupportTest
{
    private ApiControllerSupport controller;
    private HttpServletRequest request;
    private HttpServletResponse response;

    @Before
    public void setUp() throws Exception
    {
        controller = spy(ApiControllerSupport.class);
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
    }

    @Test
    public void generate()
    {
        when(request.getContextPath()).thenReturn("");
        when(request.getRequestURI()).thenReturn("/api/test");
        controller.doPost(request, response);

        verify(controller).generate(any(Request.class), any(Response.class), eq(""));
    }

    @Test
    public void generate2()
    {
        when(request.getContextPath()).thenReturn("");
        when(request.getRequestURI()).thenReturn("/api/test/");
        controller.doPost(request, response);

        verify(controller).generate(any(Request.class), any(Response.class), eq(""));
    }

    @Test
    public void subUrl()
    {
        when(request.getContextPath()).thenReturn("");
        when(request.getRequestURI()).thenReturn("/api/test/path");
        controller.doPost(request, response);

        verify(controller).generate(any(Request.class), any(Response.class), eq("path"));
    }

    @Test
    public void subUrl2()
    {
        when(request.getContextPath()).thenReturn("");
        when(request.getRequestURI()).thenReturn("/api/test/foo/bar");
        controller.doPost(request, response);

        verify(controller).generate(any(Request.class), any(Response.class), eq("foo/bar"));
    }

    @Test
    public void subUrlWithContext()
    {
        when(request.getContextPath()).thenReturn("/context");
        when(request.getRequestURI()).thenReturn("/api/test/foo/bar");
        controller.doPost(request, response);

        verify(controller).generate(any(Request.class), any(Response.class), eq("foo/bar"));
    }

    @Test
    public void doGet()
    {
        when(request.getContextPath()).thenReturn("");
        when(request.getRequestURI()).thenReturn("/api/test");
        controller.doGet(request, response);

        verify(controller).generate(any(Request.class), any(Response.class), eq(""));
    }
}