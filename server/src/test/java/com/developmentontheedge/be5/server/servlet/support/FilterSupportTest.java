package com.developmentontheedge.be5.server.servlet.support;

import com.developmentontheedge.be5.web.Request;
import com.developmentontheedge.be5.web.Response;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import java.io.IOException;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class FilterSupportTest extends WebTestSupport
{
    private FilterSupport filterSupport;
    private FilterChain filterChain;

    @Before
    public void setUp() throws Exception
    {
        filterSupport = Mockito.spy(FilterSupport.class);
        injector.injectMembers(filterSupport);
        filterChain = mock(FilterChain.class);
    }

    @Test
    public void generate() throws IOException, ServletException
    {
        when(rawRequest.getRequestURI()).thenReturn("/api/test");
        filterSupport.doFilter(rawRequest, rawResponse, filterChain);
        verify(filterSupport).filter(any(Request.class), any(Response.class), eq(filterChain));
    }
}
