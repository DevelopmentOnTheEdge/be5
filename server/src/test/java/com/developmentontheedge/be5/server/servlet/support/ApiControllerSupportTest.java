package com.developmentontheedge.be5.server.servlet.support;

import com.developmentontheedge.be5.web.Request;
import com.developmentontheedge.be5.web.Response;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class ApiControllerSupportTest extends WebTestSupport
{
    private ApiControllerSupport controller;

    @Before
    public void setUp() throws Exception
    {
        controller = Mockito.spy(ApiControllerSupport.class);
        injector.injectMembers(controller);
    }

    @Test
    public void generate()
    {
        when(rawRequest.getContextPath()).thenReturn("");
        when(rawRequest.getRequestURI()).thenReturn("/api/test");
        controller.doPost(rawRequest, rawResponse);

        verify(controller).generate(any(Request.class), any(Response.class), eq(""));
    }

    @Test
    public void generate2()
    {
        when(rawRequest.getContextPath()).thenReturn("");
        when(rawRequest.getRequestURI()).thenReturn("/api/test/");
        controller.doPost(rawRequest, rawResponse);

        verify(controller).generate(any(Request.class), any(Response.class), eq(""));
    }

    @Test
    public void subUrl()
    {
        when(rawRequest.getContextPath()).thenReturn("");
        when(rawRequest.getRequestURI()).thenReturn("/api/test/path");
        controller.doPost(rawRequest, rawResponse);

        verify(controller).generate(any(Request.class), any(Response.class), eq("path"));
    }

    @Test
    public void subUrl2()
    {
        when(rawRequest.getContextPath()).thenReturn("");
        when(rawRequest.getRequestURI()).thenReturn("/api/test/foo/bar");
        controller.doPost(rawRequest, rawResponse);

        verify(controller).generate(any(Request.class), any(Response.class), eq("foo/bar"));
    }

    @Test
    public void subUrlWithContext()
    {
        when(rawRequest.getContextPath()).thenReturn("/context");
        when(rawRequest.getRequestURI()).thenReturn("/context/api/test/foo/bar");
        controller.doPost(rawRequest, rawResponse);

        verify(controller).generate(any(Request.class), any(Response.class), eq("foo/bar"));
    }

    @Test
    public void doGet()
    {
        when(rawRequest.getContextPath()).thenReturn("");
        when(rawRequest.getRequestURI()).thenReturn("/api/test");
        controller.doGet(rawRequest, rawResponse);

        verify(controller).generate(any(Request.class), any(Response.class), eq(""));
    }
}
