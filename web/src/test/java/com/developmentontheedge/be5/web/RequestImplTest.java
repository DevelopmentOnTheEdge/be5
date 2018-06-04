package com.developmentontheedge.be5.web;

import com.developmentontheedge.be5.web.impl.RequestImpl;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class RequestImplTest
{
    private HttpServletRequest rawRequest;

    @Before
    public void setUp()
    {
        rawRequest = mock(HttpServletRequest.class);
        when(rawRequest.getSession()).thenReturn(mock(HttpSession.class));
    }

    @Test
    public void getParameterValues()
    {
        when(rawRequest.getParameterValues("ids[]")).thenReturn(new String[]{"1", "2"});
        Request req = new RequestImpl(rawRequest);

        assertEquals(Arrays.asList("1", "2"), req.getList("ids"));
    }

    @Test
    public void getParameterValuesOneValue()
    {
        when(rawRequest.getParameter("ids")).thenReturn("1");
        Request req = new RequestImpl(rawRequest);

        assertEquals(Collections.singletonList("1"), req.getList("ids"));
    }

}