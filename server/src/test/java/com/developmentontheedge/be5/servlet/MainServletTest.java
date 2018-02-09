package com.developmentontheedge.be5.servlet;

import com.developmentontheedge.be5.api.Request;
import com.developmentontheedge.be5.api.Response;
import com.developmentontheedge.be5.api.exceptions.Be5ErrorCode;
import com.developmentontheedge.be5.api.exceptions.ErrorTitles;
import com.developmentontheedge.be5.api.helpers.UserInfoHolder;
import com.developmentontheedge.be5.components.RoleSelector;
import com.developmentontheedge.be5.env.Inject;
import com.developmentontheedge.be5.env.Injector;
import com.developmentontheedge.be5.metadata.RoleType;
import com.developmentontheedge.be5.test.Be5ProjectTest;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
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
        initUserWithRoles(RoleType.ROLE_GUEST);

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
    public void testGet() throws Exception
    {
        when(request.getRequestURI()).thenReturn("/api/roleSelector");
        when(request.getParameterMap()).thenReturn(new HashMap<>());

        spyMainServlet.doFilter(request, response, mock(FilterChain.class));

        verify(writer).append(doubleQuotes("{'availableRoles':[],'selectedRoles':[],'username':'testUser'}"));
    }

    @Test
    public void testUriPrefix() throws Exception
    {
        when(request.getRequestURI()).thenReturn("/be5/api/roleSelector");
        when(request.getParameterMap()).thenReturn(new HashMap<>());

        spyMainServlet.doFilter(request, response, mock(FilterChain.class));

        verify(writer).append(doubleQuotes("{'availableRoles':[],'selectedRoles':[],'username':'testUser'}"));
    }

    @Test
    @Ignore
    public void testTemplate() throws Exception
    {
        when(request.getRequestURI()).thenReturn("/api");
        when(request.getParameterMap()).thenReturn(new HashMap<>());

        spyMainServlet.doFilter(request, response, mock(FilterChain.class));

        //verify(writer).append(contains(doubleQuotes("'detail':")));
    }

    @Test
    public void testGetError2() throws Exception
    {
        when(request.getRequestURI()).thenReturn("/api/static");
        when(request.getParameterMap()).thenReturn(new HashMap<>());

        spyMainServlet.doFilter(request, response, mock(FilterChain.class));

        verify(writer).append(doubleQuotes("{'errors':[" +
                "{'status':'500','title':'Element not found: '}" +
            "],'links':{'self':'static/'},'meta':{'_ts_':null}}"));
    }

    @Test
    public void runComponent() throws Exception
    {
        Request req = mock(Request.class);
        when(req.getRequestUri()).thenReturn("");
        Response res = mock(Response.class);

        spyMainServlet.runComponent("roleSelector", req, res);

        verify(res).sendAsRawJson(eq(new RoleSelector.RoleSelectorResponse(UserInfoHolder.getUserName(), Collections.emptyList(), Collections.emptyList())));
    }

    @Test
    public void runUnknownComponent() throws Exception
    {
        String componentId = "foo";
        Request req = mock(Request.class);
        Response res = mock(Response.class);

        spyMainServlet.runComponent(componentId, req, res);

        verify(req).setAttribute("testRequestPreprocessor", "testProject");

        res.sendError(eq(ErrorTitles.formatTitle(Be5ErrorCode.UNKNOWN_COMPONENT, componentId)));
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