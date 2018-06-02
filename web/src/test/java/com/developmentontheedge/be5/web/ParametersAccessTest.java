package com.developmentontheedge.be5.web;

import com.developmentontheedge.be5.web.impl.RequestImpl;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import javax.servlet.http.HttpServletRequest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class ParametersAccessTest
{
    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    private HttpServletRequest httpServletRequest;
    private Request req;

    @Before
    public void setUp() throws Exception
    {
        httpServletRequest = mock(HttpServletRequest.class);
        req = new RequestImpl(httpServletRequest);
    }

    @Test
    public void getNonEmpty() throws Exception
    {
        when(httpServletRequest.getParameter("name")).thenReturn("value");

        assertEquals("value", req.getNonEmpty("name"));
    }

    @Test
    public void getNonEmptyMissing() throws Exception
    {
        when(httpServletRequest.getParameter("name")).thenReturn(null);

        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("Invalid request: parameter name is missing.");

        req.getNonEmpty("name");
    }

    @Test
    public void getNonEmptyEmpty() throws Exception
    {
        when(httpServletRequest.getParameter("name")).thenReturn("  ");

        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("Invalid request: parameter name is empty.");

        req.getNonEmpty("name");
    }

    @Test
    public void getBoolean() throws Exception
    {
        when(httpServletRequest.getParameter("name")).thenReturn("true");

        assertEquals(true, req.getBoolean("name", false));
    }

    @Test
    public void getBooleanFalse() throws Exception
    {
        when(httpServletRequest.getParameter("name")).thenReturn("yes");

        assertEquals(false, req.getBoolean("name", false));
    }
}