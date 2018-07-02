package com.developmentontheedge.be5.web;

import com.developmentontheedge.be5.web.impl.RequestImpl;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import javax.servlet.http.HttpServletRequest;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class ParametersAccessTest
{
    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    private HttpServletRequest httpServletRequest;
    private Request req;

    @Before
    public void setUp()
    {
        httpServletRequest = mock(HttpServletRequest.class);
        req = new RequestImpl(httpServletRequest);
    }

    @Test
    public void getNonEmpty()
    {
        when(httpServletRequest.getParameter("name")).thenReturn("value");

        assertEquals("value", req.getNonEmpty("name"));
    }

    @Test
    public void getNonEmptyMissing()
    {
        when(httpServletRequest.getParameter("name")).thenReturn(null);

        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("Invalid request: parameter name is missing.");

        req.getNonEmpty("name");
    }

    @Test
    public void getNonEmptyEmpty()
    {
        when(httpServletRequest.getParameter("name")).thenReturn("  ");

        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("Invalid request: parameter name is empty.");

        req.getNonEmpty("name");
    }

    @Test
    public void getBoolean()
    {
        when(httpServletRequest.getParameter("name")).thenReturn("true");

        assertEquals(true, req.getBoolean("name", false));
    }

    @Test
    public void getBooleanFalse()
    {
        when(httpServletRequest.getParameter("name")).thenReturn("yes");

        assertEquals(false, req.getBoolean("name", false));
    }

    @Test
    public void getOrDefault()
    {
        when(httpServletRequest.getParameter("name")).thenReturn("bar");

        assertEquals("bar", req.getOrDefault("name", "foo"));
    }

    @Test
    public void getOrDefault_null()
    {
        assertEquals("foo", req.getOrDefault("name", "foo"));
    }

    @Test
    public void getOrEmpty()
    {
        when(httpServletRequest.getParameter("name")).thenReturn("bar");

        assertEquals("bar", req.getOrEmpty("name"));
    }

    @Test
    public void getOrEmpty_default()
    {
        assertEquals("", req.getOrEmpty("name"));
    }

    @Test
    public void getInteger()
    {
        when(httpServletRequest.getParameter("name")).thenReturn("123");

        assertEquals(123, (int)req.getInteger("name"));
    }

    @Test
    public void getLong()
    {
        when(httpServletRequest.getParameter("name")).thenReturn("123");

        assertEquals(123L, (long)req.getLong("name"));
    }

    @Test
    public void getInteger_null()
    {
        assertEquals(null, req.getInteger("name"));
    }
}