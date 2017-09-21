package com.developmentontheedge.be5.servlet;

import com.developmentontheedge.be5.api.Request;
import com.developmentontheedge.be5.api.Response;
import com.developmentontheedge.be5.api.exceptions.Be5ErrorCode;
import com.developmentontheedge.be5.api.exceptions.ErrorMessages;
import com.developmentontheedge.be5.env.Inject;
import com.developmentontheedge.be5.env.Injector;
import com.developmentontheedge.be5.test.Be5ProjectTest;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class MainServletTest extends Be5ProjectTest
{
    @Inject private Injector injector;

    private HttpServletRequest request = mock(HttpServletRequest.class);
    private HttpServletResponse response = mock(HttpServletResponse.class);
    private PrintWriter writer = mock(PrintWriter.class);
    private MainServlet spyMainServlet;

    @Before
    public void init() throws IOException
    {
        spyMainServlet = spy(MainServlet.class);
        when(spyMainServlet.getInjector()).thenReturn(injector);

        request = mock(HttpServletRequest.class);
        when(request.getMethod()).thenReturn("GET");
        when(request.getSession()).thenReturn(mock(HttpSession.class));

        writer = mock(PrintWriter.class);
        response = mock(HttpServletResponse.class);
        when(response.getWriter()).thenReturn(writer);
    }

    @Test
    @Ignore//todo use another component
    public void testGet() throws Exception
    {
        when(request.getRequestURI()).thenReturn("/api/static/info.be");
        when(request.getParameterMap()).thenReturn(new HashMap<>());

        spyMainServlet.doGet(request, response);

        verify(writer).append(doubleQuotes("{'type':'static','value':'<h1>Info</h1><p>Test text.</p>'}"));
    }

    @Test
    @Ignore//todo use another component
    public void testUriPrefix() throws Exception
    {
        when(request.getRequestURI()).thenReturn("/be5/api/static/info.be");
        when(request.getParameterMap()).thenReturn(new HashMap<>());

        spyMainServlet.doGet(request, response);

        verify(writer).append(doubleQuotes("{'type':'static','value':'<h1>Info</h1><p>Test text.</p>'}"));
    }

    @Test
    public void testGetError() throws Exception
    {
        when(request.getRequestURI()).thenReturn("/api");
        when(request.getParameterMap()).thenReturn(new HashMap<>());

        spyMainServlet.doPost(request, response);

        verify(writer).append(doubleQuotes("{'type':'error','value':{'code':'UNKNOWN_COMPONENT','message':''}}"));
    }

    @Test
    public void testGetError2() throws Exception
    {
        when(request.getRequestURI()).thenReturn("/api/static");
        when(request.getParameterMap()).thenReturn(new HashMap<>());

        spyMainServlet.doPost(request, response);

        verify(writer).append(doubleQuotes("{'type':'error','value':{'code':'NOT_FOUND','message':''}}"));
    }

    @Test
    @Ignore//todo use another component
    public void runComponent() throws Exception
    {
        Request req = mock(Request.class);
        when(req.getRequestUri()).thenReturn("info.be");
        Response res = mock(Response.class);

        spyMainServlet.runComponent("static", req, res);

        verify(res).sendAsJson(eq("static"), eq("<h1>Info</h1><p>Test text.</p>"));
    }

    @Test
    public void runUnknownComponent() throws Exception
    {
        String componentId = "foo";
        Request req = mock(Request.class);
        Response res = mock(Response.class);

        spyMainServlet.runComponent(componentId, req, res);

        res.sendError(eq(ErrorMessages.formatMessage(Be5ErrorCode.UNKNOWN_COMPONENT, componentId)));
    }

    @Test
    public void testSimplify() throws Exception
    {
        Map<String, String[]> in = new HashMap<>();
        in.put("testP", new String[]{"p1", "p2"});
        in.put("testZ", new String[]{"z1"});

        Map<String, String> out = new MainServlet().simplify(in);
        assertEquals("z1", out.get("testZ"));
    }

}