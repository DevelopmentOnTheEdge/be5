package com.developmentontheedge.be5.servlet;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
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
import com.developmentontheedge.be5.env.Be5;
import com.developmentontheedge.be5.env.Injector;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;


public class MainServlet extends HttpServlet
{
    private static final Logger log = Logger.getLogger(MainServlet.class.getName());

    private Pattern uriPattern = Pattern.compile("(/.*)?/api/(.*)");

    private Injector injector;

    //TODO private final DaemonStarter starter;

    @Override
    public void init(ServletConfig config) throws ServletException
    {
        super.init(config);
        injector = Be5.createInjector();
        injector.getDatabaseService();
    }

    public Injector getInjector()
    {
        return injector;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        respond(request, response, request.getMethod(), request.getRequestURI(), request.getParameterMap());
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        respond(request, response, request.getMethod(), request.getRequestURI(), request.getParameterMap());
    }

    /**
     * The general routing method. Tries to determine and find a component using a given request URI.
     * Generation of response is delegated to a found component.
     */
    private void respond(HttpServletRequest request, HttpServletResponse response, String method, String requestUri, Map<String, String[]> parameters)
    {
        String origin = request.getHeader("Origin");
        // TODO test origin

        response.addHeader("Access-Control-Allow-Credentials", "true");
        response.addHeader("Access-Control-Allow-Origin", origin);
        response.addHeader("Access-Control-Allow-Methods", "POST, GET");
        response.addHeader("Access-Control-Max-Age", "1728000");

        response.setHeader("Expires", "Tue, 03 Jul 2001 06:00:00 GMT");
        response.setDateHeader("Last-Modified", new Date().getTime());
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0, post-check=0, pre-check=0");
        response.setHeader("Pragma", "no-cache");

        Response res = new ResponseImpl(response);

        Matcher matcher = uriPattern.matcher(requestUri);
        if (!matcher.matches())
        {
            res.sendError(Be5Exception.unknownComponent(requestUri));
            return;
        }

        String[] uriParts = requestUri.split("/");
        int ind = 1;

        while (!"api".equals(uriParts[ind]) && ind + 1 < uriParts.length)
        {
            ind++;
        }

        String subRequestUri = Joiner.on('/').join(Iterables.skip(Arrays.asList(uriParts), ind + 2));
        Request req = new RequestImpl(request, subRequestUri, simplify(parameters));

        if (UserInfoHolder.getUserInfo() == null)
        {
            injector.getLoginService().initGuest(req);
        }

        String componentId = uriParts[ind + 1];

        runComponent(componentId, req, res);
    }

    void runComponent(String componentId, Request req, Response res)
    {
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
