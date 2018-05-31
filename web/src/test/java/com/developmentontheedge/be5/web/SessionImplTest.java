package com.developmentontheedge.be5.web;

import com.developmentontheedge.be5.web.impl.SessionImpl;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.http.HttpSession;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SessionImplTest
{
    private HttpSession httpSessionMock;
    private SessionImpl session;

    @Before
    public void setUp() throws Exception
    {
        httpSessionMock = mock(HttpSession.class);
        session = new SessionImpl(httpSessionMock);
    }

    @Test
    public void get() throws Exception
    {
        when(httpSessionMock.getAttribute("name")).thenReturn("value");
        assertEquals("value", session.get("name"));
    }

    @Test
    public void getOrDefault() throws Exception
    {
        when(httpSessionMock.getAttribute("name")).thenReturn("value");
        assertEquals("value", session.getOrDefault("name", "bar"));

        assertEquals("bar", session.getOrDefault("foo", "bar"));
    }

    @Test
    public void set() throws Exception
    {
        session.set("name", "value");
        verify(httpSessionMock).setAttribute("name", "value");
    }

    @Test
    public void remove() throws Exception
    {
        session.remove("name");
        verify(httpSessionMock).removeAttribute("name");
    }

    @Test
    public void getAllAttributes() throws Exception
    {
        when(httpSessionMock.getAttributeNames()).thenReturn(Collections.enumeration(Arrays.asList("name", "foo")));
        when(httpSessionMock.getAttribute("name")).thenReturn("value");
        when(httpSessionMock.getAttribute("foo")).thenReturn("bar");

        Map<String, Object> attributes = session.getAttributes();
        assertEquals(2, attributes.size());
        assertEquals("value", attributes.get("name"));
        assertEquals("bar", attributes.get("foo"));
    }

    @Test
    public void getAttributeNames() throws Exception
    {
        when(httpSessionMock.getAttributeNames()).thenReturn(Collections.enumeration(Arrays.asList("name", "foo")));

        assertEquals(Arrays.asList("name", "foo"), session.getAttributeNames());
    }
}