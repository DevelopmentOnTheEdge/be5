package com.developmentontheedge.be5.web.impl;

import com.developmentontheedge.be5.web.Request;
import com.developmentontheedge.be5.web.Response;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provides;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.PrintWriter;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ControllerSupportTest
{
    private HttpServletRequest rawRequest;
    private HttpServletResponse rawResponse;
    private PrintWriter writer;
    private TestController testController;

    @Before
    public void init() throws Exception
    {
        writer = mock(PrintWriter.class);

        rawResponse = mock(HttpServletResponse.class);
        when(rawResponse.getWriter()).thenReturn(writer);

        rawRequest = mock(HttpServletRequest.class);
        when(rawRequest.getSession()).thenReturn(mock(HttpSession.class));

        testController = new TestController();
        Injector injector = Guice.createInjector(new TestWebModule());
        injector.injectMembers(testController);
    }

    @Test
    public void doGet()
    {
        when(rawRequest.getParameter("name")).thenReturn("value");
        testController.doGet(rawRequest, rawResponse);
        verify(rawResponse).setContentType("text/html;charset=UTF-8");
        verify(writer).append("value test");
    }

    @Test
    public void doPost()
    {
        when(rawRequest.getParameter("name")).thenReturn("value");
        testController.doPost(rawRequest, rawResponse);
        verify(rawResponse).setContentType("text/html;charset=UTF-8");
        verify(writer).append("value test");
    }

    class TestController extends ControllerSupport
    {
        @Override
        public void generate(Request req, Response res)
        {
            res.sendHtml(req.getNonEmpty("name") + " test");
        }
    }

    class TestWebModule extends AbstractModule
    {
        @Provides
        Request provideUserInfo()
        {
            return new RequestImpl(rawRequest);
        }
    }
}
