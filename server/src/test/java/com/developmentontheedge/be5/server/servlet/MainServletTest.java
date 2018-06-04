package com.developmentontheedge.be5.server.servlet;

import com.developmentontheedge.be5.test.ServerBe5ProjectTest;


public class MainServletTest extends ServerBe5ProjectTest
{
//    @Inject private Injector injector;
//
//    private HttpServletRequest request = mock(HttpServletRequest.class);
//    private HttpServletResponse response = mock(HttpServletResponse.class);
//    private PrintWriter writer = mock(PrintWriter.class);
//    private MainServlet spyMainServlet;
//
//    @Before
//    public void init() throws IOException
//    {
//        initUserWithRoles(RoleType.ROLE_GUEST);
//
//        spyMainServlet = spy(MainServlet.class);
//        when(spyMainServlet.getInjector()).thenReturn(injector);
//
//        request = mock(HttpServletRequest.class);
//        when(request.getMethod()).thenReturn("GET");
//        when(request.getSession()).thenReturn(mock(HttpSession.class));
//
//        writer = mock(PrintWriter.class);
//        response = mock(HttpServletResponse.class);
//        when(response.getWriter()).thenReturn(writer);
//    }
//
//    @Test
//    public void testGet() throws Exception
//    {
//        when(request.getRequestURI()).thenReturn("/api/static/info.be");
//        when(request.getParameterMap()).thenReturn(new HashMap<>());
//
//        spyMainServlet.doFilter(request, response, mock(FilterChain.class));
//
//        verify(writer).append(doubleQuotes("{'data':{'attributes':{" +
//                "'content':'<h1>Info</h1><p>Test text.</p>','title':''}," +
//                "'links':{'self':'static/info.be'}," +
//                "'type':'static'},'meta':{'_ts_':null}}"));
//    }
//
//    @Test
//    public void testUriPrefix() throws Exception
//    {
//        when(request.getRequestURI()).thenReturn("/be5/api/static/info.be");
//        when(request.getParameterMap()).thenReturn(new HashMap<>());
//
//        spyMainServlet.doFilter(request, response, mock(FilterChain.class));
//
//        verify(writer).append(doubleQuotes("{'data':{'attributes':{" +
//                "'content':'<h1>Info</h1><p>Test text.</p>','title':''}," +
//                "'links':{'self':'static/info.be'}," +
//                "'type':'static'},'meta':{'_ts_':null}}"));
//    }
//
//    @Test
//    @Ignore
//    public void testTemplate() throws Exception
//    {
//        when(request.getRequestURI()).thenReturn("/api");
//        when(request.getParameterMap()).thenReturn(new HashMap<>());
//
//        spyMainServlet.doFilter(request, response, mock(FilterChain.class));
//
//        //verify(writer).append(contains(doubleQuotes("'detail':")));
//    }
//
//    @Test
//    public void testGetError2() throws Exception
//    {
//        when(request.getRequestURI()).thenReturn("/api/static");
//        when(request.getParameterMap()).thenReturn(new HashMap<>());
//
//        spyMainServlet.doFilter(request, response, mock(FilterChain.class));
//
//        verify(writer).append(doubleQuotes("{'errors':[" +
//                "{'links':{'self':'static/'},'status':'500','title':'Element not found: '}" +
//            "],'meta':{'_ts_':null}}"));
//    }
//
//    @Test
//    public void runComponent()
//    {
//        Request req = mock(Request.class);
//        when(req.getRequestUri()).thenReturn("info.be");
//        Response res = mock(Response.class);
//
//        spyMainServlet.runComponent("static", req, res);
//
//        verify(res).sendAsJson(any(ResourceData.class), any(Map.class));
//    }
//
//    @Test
//    public void runUnknownComponent()
//    {
//        String componentId = "foo";
//        Request req = mock(Request.class);
//        Response res = mock(Response.class);
//
//        spyMainServlet.runComponent(componentId, req, res);
//
//        verify(req).setAttribute("testRequestPreprocessor", Stage.TEST.toString());
//
//        verify(res).sendError(eq(Be5Exception.unknownComponent("foo")));
//    }

}