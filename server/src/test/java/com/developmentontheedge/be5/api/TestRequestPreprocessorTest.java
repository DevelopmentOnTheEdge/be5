package com.developmentontheedge.be5.api;

import com.google.inject.servlet.GuiceFilter;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.*;

public class TestRequestPreprocessorTest
{
    @Test
    @Ignore
    public void runUnknownComponent()
    {
        new GuiceFilter();
//        String componentId = "foo";
//        Request req = mock(Request.class);
//        Response res = mock(Response.class);
//
//        spyMainServlet.runComponent(componentId, req, res);
//
//        verify(req).setAttribute("testRequestPreprocessor", Stage.TEST.toString());
//
//        verify(res).sendError(eq(Be5Exception.unknownComponent("foo")));
    }
}