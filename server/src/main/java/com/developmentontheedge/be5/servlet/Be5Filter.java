package com.developmentontheedge.be5.servlet;

import com.developmentontheedge.be5.api.Response;
import com.developmentontheedge.be5.api.impl.ResponseImpl;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;


public class Be5Filter implements Filter
{
    private static final Logger log = Logger.getLogger(Be5Filter.class.getName());

    //private Pattern uriPattern = Pattern.compile("(/.*)?/api/(.*)");

    //private Injector injector;
    private ServletContext servletContext;

    //TODO private final DaemonStarter starter;

    @Override
    public void init(FilterConfig filterConfig)
    {
        servletContext = filterConfig.getServletContext();
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException
    {
        HttpServletRequest req = (HttpServletRequest) request;

//        if (!respond(req, (HttpServletResponse)response, req.getMethod(),req.getRequestURI(), req.getParameterMap())) {
//            chain.doFilter(request, response);
//        }
    }

    @Override
    public void destroy()
    {
        // nothing to do
    }

    /**
     * The general routing method. Tries to determine and find a component using a given request URI.
     * Generation of response is delegated to a found component.
     */
//    private boolean respond(HttpServletRequest request, HttpServletResponse response, String method, String requestUri, Map<String, String[]> parameters)
//    {
//        Matcher matcher = uriPattern.matcher(requestUri);
//        if (!matcher.matches())
//        {
//            return getTemplate(request, response, requestUri, parameters);
//        }
//
//        String[] uriParts = requestUri.split("/");
//        int ind = 1;
//
//        while (!"api".equals(uriParts[ind]) && ind + 1 < uriParts.length)
//        {
//            ind++;
//        }
//
//        String subRequestUri = Joiner.on('/').join(Iterables.skip(Arrays.asList(uriParts), ind + 2));
//        String componentId = uriParts[ind + 1];
//
//        Request req = new RequestImpl(request, subRequestUri, arrayToList(parameters));
//        UserInfoHolder.setRequest(req);
//        runComponent(componentId, req, getResponse(request, response));
//        return true;
//    }

    private Response getResponse(HttpServletRequest request, HttpServletResponse response)
    {
        String origin = request.getHeader("Origin");// TODO test origin

        response.addHeader("Access-Control-Allow-Credentials", "true");
        response.addHeader("Access-Control-Allow-Origin", origin);
        response.addHeader("Access-Control-Allow-Methods", "POST, GET");
        response.addHeader("Access-Control-Max-Age", "1728000");

        response.setHeader("Pragma", "no-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);

        return new ResponseImpl(response);
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

    Map<String, Object> arrayToList(Map<String, String[]> parameters)
    {
        Map<String, Object> map = new HashMap<>();

        for( Map.Entry<String, String[]> parameter : parameters.entrySet() )
        {
            if( parameter.getValue().length == 1 )
            {
                map.put( parameter.getKey(), parameter.getValue()[0] );
            }
            else
            {
                map.put( parameter.getKey(), Arrays.asList(parameter.getValue()) );
            }
        }

        return map;
    }

}
