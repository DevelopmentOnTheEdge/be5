package com.developmentontheedge.be5.servlet;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.Filter;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.developmentontheedge.be5.api.Component;
import com.developmentontheedge.be5.api.Request;
import com.developmentontheedge.be5.api.RequestPreprocessor;
import com.developmentontheedge.be5.api.Response;
import com.developmentontheedge.be5.api.exceptions.Be5Exception;
import com.developmentontheedge.be5.api.helpers.UserInfoHolder;
import com.developmentontheedge.be5.api.impl.RequestImpl;
import com.developmentontheedge.be5.api.impl.ResponseImpl;
import com.developmentontheedge.be5.components.TemplateProcessor;
import com.developmentontheedge.be5.env.Be5;
import com.developmentontheedge.be5.env.Injector;
import com.developmentontheedge.be5.env.Stage;
import com.developmentontheedge.be5.env.impl.YamlBinder;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;


public class MainServlet implements Filter
{
    private static final Logger log = Logger.getLogger(MainServlet.class.getName());

    private Pattern uriPattern = Pattern.compile("(/.*)?/api/(.*)");

    private Injector injector;
    private ServletContext servletContext;

    //TODO private final DaemonStarter starter;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException
    {
        servletContext = filterConfig.getServletContext();

        boolean mode = MainServlet.class.getClassLoader().getResource("dev.yaml") != null;

        injector = Be5.createInjector(mode ? Stage.DEVELOPMENT : Stage.PRODUCTION, new YamlBinder());

        injector.getDatabaseService();
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException
    {
        HttpServletRequest req = (HttpServletRequest) request;

        if (!respond(req, (HttpServletResponse)response, req.getMethod(),req.getRequestURI(), req.getParameterMap())) {
            chain.doFilter(request, response);
        }
    }

    @Override
    public void destroy()
    {
        // nothing to do
    }

    public Injector getInjector()
    {
        return injector;
    }

    /**
     * The general routing method. Tries to determine and find a component using a given request URI.
     * Generation of response is delegated to a found component.
     */
    private boolean respond(HttpServletRequest request, HttpServletResponse response, String method, String requestUri, Map<String, String[]> parameters)
    {
        String origin = request.getHeader("Origin");
        // TODO test origin

        response.addHeader("Access-Control-Allow-Credentials", "true");
        response.addHeader("Access-Control-Allow-Origin", origin);
        response.addHeader("Access-Control-Allow-Methods", "POST, GET");
        response.addHeader("Access-Control-Max-Age", "1728000");

        response.setHeader("Pragma", "no-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);

        Response res = new ResponseImpl(response);

        Matcher matcher = uriPattern.matcher(requestUri);
        if (!matcher.matches())
        {
            // This prevents triggering engine executions for resource URLs
            log.log(Level.INFO, requestUri + " ContextPath=" + request.getContextPath());//todo remove

            String contextPath = request.getContextPath();
            if (requestUri.startsWith( contextPath + (contextPath.endsWith("/") ? "" : "/") + "static/")
                    || requestUri.contains("favicon.ico"))//|| requestUri.startsWith("/static/")
            {
                return false;
            }
            String templateComponentID = "templateProcessor";
            if(!injector.hasComponent(templateComponentID))
            {
                templateComponentID = "defaultTemplateProcessor";
            }

            RequestImpl req = new RequestImpl(request, requestUri, simplify(parameters));
            UserInfoHolder.setRequest(req);
            runTemplateProcessor(templateComponentID, req, res);
            return true;
        }

        String[] uriParts = requestUri.split("/");
        int ind = 1;

        while (!"api".equals(uriParts[ind]) && ind + 1 < uriParts.length)
        {
            ind++;
        }

        String subRequestUri = Joiner.on('/').join(Iterables.skip(Arrays.asList(uriParts), ind + 2));
        String componentId = uriParts[ind + 1];

        Request req = new RequestImpl(request, subRequestUri, simplify(parameters));
        UserInfoHolder.setRequest(req);
        runComponent(componentId, req, res);
        return true;
    }

    private void runTemplateProcessor(String componentId, Request req, Response res)
    {
        if (UserInfoHolder.getUserInfo() == null)
        {
            injector.getLoginService().initGuest(req);
        }

        try
        {
            runRequestPreprocessors(componentId, req, res);
            new TemplateProcessor(servletContext)
                    .generate(req, res, injector);
        }

        catch ( Be5Exception ex )
        {
            if(ex.getCode().isInternal() || ex.getCode().isAccessDenied())
            {
                log.log(Level.SEVERE, ex.getMessage(), ex);
            }

            if(ex.getCode().isAccessDenied())
            {
                res.sendAccessDenied(ex);
            }
            else
            {
                res.sendError(ex);
            }
        }
        catch ( Throwable e )
        {
            log.log(Level.SEVERE, e.getMessage(), e);
            res.sendError(Be5Exception.internal(e));
        }
    }

    void runComponent(String componentId, Request req, Response res)
    {
        if (UserInfoHolder.getUserInfo() == null)
        {
            injector.getLoginService().initGuest(req);
        }

        try
        {
            runRequestPreprocessors(componentId, req, res);
            Component component = getInjector().getComponent(componentId);
            component.generate( req, res, getInjector() );
        }

        catch ( Be5Exception ex )
        {
            if(ex.getCode().isInternal() || ex.getCode().isAccessDenied())
            {
                log.log(Level.SEVERE, ex.getMessage(), ex);
            }

            if(ex.getCode().isAccessDenied())
            {
                res.sendAccessDenied(ex);
            }
            else
            {
                res.sendError(ex);
            }
        }
        catch ( Throwable e )
        {
            log.log(Level.SEVERE, e.getMessage(), e);
            res.sendError(Be5Exception.internal(e));
        }
    }

    private void runRequestPreprocessors(String componentId, Request req, Response res)
    {
        List<RequestPreprocessor> requestPreprocessors = getInjector().getRequestPreprocessors();
        for (RequestPreprocessor preprocessor : requestPreprocessors)
        {
            preprocessor.preprocessUrl(componentId, req, res);
        }
    }

    ///////////////////////////////////////////////////////////////////
    // web socket
    //


//    public void onWsOpen(Session session)
//    {
//        String componentName = session.getPathParameters().get( "component" );
//        WebSocketComponent component;
//        try
//        {
//            component = createWebSocketComponent( componentName );
//        }
//        catch( Be5Exception ex )
//        {
//            try
//            {
//                session.close( new CloseReason( CloseCodes.getCloseCode( CloseCodes.CANNOT_ACCEPT.getCode() ), "Invalid component: "
//                        + componentName ) );
//            }
//            catch( IOException e )
//            {
//                // ignore
//            }
//            return;
//        }
//        component.onOpen( new WebSocketContextImpl( session ), getInjector().getServiceProvider() );
//    }
//
//    public void onWsMessage(byte[] msg, Session session)
//    {
//        String component = session.getPathParameters().get( "component" );
//        createWebSocketComponent( component ).onMessage( new WebSocketContextImpl( session ), getInjector().getServiceProvider(), msg );
//    }
//
//    public void onWsClose(Session session)
//    {
//        String component = session.getPathParameters().get( "component" );
//        createWebSocketComponent( component ).onClose( new WebSocketContextImpl( session ), getInjector().getServiceProvider() );
//    }
//
//    /**
//     * Returns a created component.
//     */
//    private WebSocketComponent createWebSocketComponent(String componentId)
//    {
//        try
//        {
//            Class<?> klass = loadedWsClasses.computeIfAbsent( componentId, this::loadWebSocketComponentClass );
//            if( klass == null )
//                throw Be5Exception.unknownComponent( componentId );
//            return (WebSocketComponent)klass.newInstance();
//        }
//        catch( InstantiationException | IllegalAccessException | ClassCastException e )
//        {
//            throw Be5Exception.internal( e );
//        }
//    }
//
//    /**
//     * Tries to find a web socket component by its ID
//     * and returns a referenced class or null if can't find a component declaration.
//     *
//     * @param componentId
//     * @throws AssertionError if a found component declaration has incorrect class name
//     */
//    private Class<?> loadWebSocketComponentClass(String componentId)
//    {
//        TO/** DO
//        IConfigurationElement componentDeclaration = findDeclaration( componentId, "com.developmentontheedge.be5.websocketcomponent", "component" );
//
//        if( componentDeclaration == null )
//            return null;
//
//        return loadClass( componentId, componentDeclaration );
//        */
//
//        return null;
//    }

    ///////////////////////////////////////////////////////////////////
    // misc
    //

    /**
     * Transforms a parameters from a multimap to a simple map,
     * ignoring parameters with several values.
     */
    Map<String, String> simplify(Map<String, String[]> parameters)
    {
        Map<String, String> simplified = new HashMap<>();

        for( Map.Entry<String, String[]> parameter : parameters.entrySet() )
        {
            if( parameter.getValue().length == 1 )
            {
                simplified.put( parameter.getKey(), parameter.getValue()[0] );
            }
        }

        return simplified;
    }

}
