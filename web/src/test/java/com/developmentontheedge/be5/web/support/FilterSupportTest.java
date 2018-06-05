package com.developmentontheedge.be5.web.support;

import com.developmentontheedge.be5.web.Request;
import com.developmentontheedge.be5.web.Response;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class FilterSupportTest
{
    private FilterSupport filterSupport;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private FilterChain filterChain;

    @Before
    public void setUp() throws Exception
    {
        filterSupport = spy(FilterSupport.class);
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        filterChain = mock(FilterChain.class);
    }

    @Test
    public void generate() throws IOException, ServletException
    {
        when(request.getRequestURI()).thenReturn("/api/test");
        filterSupport.doFilter(request, response, filterChain);

        verify(filterSupport).filter(any(Request.class), any(Response.class), eq(filterChain));
    }

    @Test
    public void filter() throws Exception
    {

    }

}