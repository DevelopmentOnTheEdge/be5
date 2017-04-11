package com.developmentontheedge.be5.servlet;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.websocket.CloseReason;
import javax.websocket.Session;
import javax.websocket.CloseReason.CloseCodes;

import com.developmentontheedge.be5.api.Component;
import com.developmentontheedge.be5.api.ComponentProvider;
import com.developmentontheedge.be5.api.Request;
import com.developmentontheedge.be5.api.ServiceProvider;
import com.developmentontheedge.be5.api.WebSocketComponent;
import com.developmentontheedge.be5.api.exceptions.Be5Exception;
import com.developmentontheedge.be5.api.exceptions.impl.Be5ErrorCode;
import com.developmentontheedge.be5.api.helpers.UserInfoHolder;
import com.developmentontheedge.be5.api.impl.MainComponentProvider;
import com.developmentontheedge.be5.api.impl.MainServiceProvider;
import com.developmentontheedge.be5.api.impl.RequestImpl;
import com.developmentontheedge.be5.api.impl.ResponseImpl;
import com.developmentontheedge.be5.api.impl.WebSocketContextImpl;
import com.developmentontheedge.be5.api.services.DatabaseService;
import com.developmentontheedge.be5.env.ServiceLoader;
import com.developmentontheedge.be5.model.UserInfo;
import com.developmentontheedge.be5.util.Delegator;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;


/**
 * Servlet implementation class MainServlet
 */
//@WebServlet(description = "Routing requests", urlPatterns = { "/api/*" }, loadOnStartup = 1)
public class MainServlet extends HttpServlet 
{
    private static final Logger log = Logger.getLogger(MainServlet.class.getName());

    protected Pattern uriPattern = Pattern.compile( "(/.*)?/api/(.*)" );

    private static final long serialVersionUID = 1L;

    private static final ComponentProvider loadedClasses = new MainComponentProvider();

    /**
     * Classes cache: webSocketComponentId->class.
     */
    private static final Map<String, Class<?>> loadedWsClasses = new ConcurrentHashMap<>();

    //TODO private final DaemonStarter starter;
    private static final ServiceLoader serviceLoader = new ServiceLoader();
    private static final ServiceProvider serviceProvider = new MainServiceProvider();

    ///////////////////////////////////////////////////////////////////
    // init
    //

    @Override
    public void init(ServletConfig config) throws ServletException 
    {
        super.init(config);

        try
        {
            serviceLoader.load(serviceProvider, loadedClasses);
        }
        catch (IOException e)
        {
            throw Be5ErrorCode.INTERNAL_ERROR.rethrow(log, e);
        }

        WebSocketServlet.setMain(this);
    }

    ///////////////////////////////////////////////////////////////////
    // responses
    //
    
    /**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
	    respond(request, response);
	}
	
	/**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
     */
    @Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
    {
        respond(request, response);
    }
    
    private void respond(HttpServletRequest request, HttpServletResponse response) throws ServletException 
    {
        try
        {
            respond(request, response, request.getMethod(), request.getRequestURI(), request.getParameterMap());
	    }
        catch (Exception e)
        {
	        throw new ServletException(e);
	    }
        
        return;
    }

    /**
     * The general routing method. Tries to determine and find a component using a given request URI.
     * Generation of response is delegated to a found component.
     *
     * @throws RuntimeException
     */
    public void respond(Object requestObj, Object responseObj, String method, String requestUri, Map<String, String[]> parameters)
    {
        HttpServletRequest request = Delegator.on( requestObj, HttpServletRequest.class );
        HttpServletResponse response = Delegator.on( responseObj, HttpServletResponse.class );

        String origin = request.getHeader( "Origin" );
        // TODO test origin

        response.addHeader( "Access-Control-Allow-Credentials", "true" );
        response.addHeader( "Access-Control-Allow-Origin", origin );
        response.addHeader( "Access-Control-Allow-Methods", "POST, GET" );
        response.addHeader( "Access-Control-Max-Age", "1728000" );

        Matcher matcher = uriPattern.matcher( requestUri );
        if( !matcher.matches() )
        {
            trySendError( HttpServletResponse.SC_NOT_FOUND, response );
            return;
        }

        String[] uriParts = requestUri.split("/");
        int ind = 1;

        while(!"api".equals(uriParts[ind]) && ind+1 < uriParts.length )
        {
            ind++;
        }
        
        String componentId = uriParts[++ind];
        Component component;
        try
        {
            component = createComponent( componentId );
        }
        catch( Be5Exception e )
        {
            trySendError( HttpServletResponse.SC_INTERNAL_SERVER_ERROR, response );
            return;
        }

        String subRequestUri = Joiner.on('/').join( Iterables.skip( Arrays.asList(uriParts), ind+1));
        Request req = new RequestImpl( request, subRequestUri, simplify( parameters ) );

        // do some preprocessing using
        // a registered ('system -> REQUEST_PREPROCESSOR') request preprocessor
        try
        {
            preprocessRequest( request, serviceProvider.getDatabaseService(), UserInfoHolder.getUserInfo(), "qps" );
        }
        catch( Exception e ) // ignore checkers' warnings, we want to catch them all
        {
            serviceProvider.getLogger().error(e);
            trySendError( HttpServletResponse.SC_INTERNAL_SERVER_ERROR, response );
            return;
        }

        try
        {
            serviceLoader.configureComponentIfConfigurable(component, componentId);
            component.generate( req, new ResponseImpl(response), serviceProvider );
        }
        catch ( RuntimeException | Error e )
        {
            serviceProvider.getLogger().error(e);
            throw e;
        }
    }

    void preprocessRequest(HttpServletRequest request, DatabaseService databaseService, UserInfo userInfo, String url)
            throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException
    {
        /** TODO - remove?
    	String className = Component? Utils.getSystemSetting( "REQUEST_PREPROCESSOR" );

        if( className != null )
        {
            RequestPreprocessor preprocessor = Classes.tryLoad( className, RequestPreprocessor.class )
                    .getConstructor(DatabaseService.class, UserInfo.class ).newInstance( databaseService, userInfo );

            preprocessor.preprocessUrl( request, url );
        }*/
    }

    /**
     * Returns a created component.
     */
    private Component createComponent(String componentId)
    {
        try
        {
            Class<?> klass = loadedClasses.get(componentId);
            return (Component)klass.newInstance();
        }
        catch( InstantiationException | IllegalAccessException | ClassCastException e )
        {
            throw Be5ErrorCode.INTERNAL_ERROR.rethrow(log, e);
        }
    }

    private void trySendError(int errorCode, HttpServletResponse response)
    {
        try
        {
            response.sendError( errorCode );
        }
        catch( IOException e )
        {
            log.log(Level.SEVERE,"response.sendError", e);
        }
    }

    ///////////////////////////////////////////////////////////////////
    // web socket
    //


    public void onWsOpen(Object sessionObj)
    {
        Session session = Delegator.on( sessionObj, Session.class );
        String componentName = session.getPathParameters().get( "component" );
        WebSocketComponent component;
        try
        {
            component = createWebSocketComponent( componentName );
        }
        catch( Be5Exception ex )
        {
            try
            {
                session.close( new CloseReason( CloseCodes.getCloseCode( CloseCodes.CANNOT_ACCEPT.getCode() ), "Invalid component: "
                        + componentName ) );
            }
            catch( IOException e )
            {
                // ignore
            }
            return;
        }
        component.onOpen( new WebSocketContextImpl( session ), serviceProvider );
    }

    public void onWsMessage(byte[] msg, Object sessionObj)
    {
        Session session = Delegator.on( sessionObj, Session.class );
        String component = session.getPathParameters().get( "component" );
        createWebSocketComponent( component ).onMessage( new WebSocketContextImpl( session ), serviceProvider, msg );
    }

    public void onWsClose(Object sessionObj)
    {
        Session session = Delegator.on( sessionObj, Session.class );
        String component = session.getPathParameters().get( "component" );
        createWebSocketComponent( component ).onClose( new WebSocketContextImpl( session ), serviceProvider );
    }

    /**
     * Returns a created component.
     */
    private WebSocketComponent createWebSocketComponent(String componentId)
    {
        try
        {
            Class<?> klass = loadedWsClasses.computeIfAbsent( componentId, this::loadWebSocketComponentClass );
            if( klass == null )
                throw Be5ErrorCode.UNKNOWN_COMPONENT.exception( componentId );
            return (WebSocketComponent)klass.newInstance();
        }
        catch( InstantiationException | IllegalAccessException | ClassCastException e )
        {
            throw Be5Exception.internal( e );
        }
    }

    /**
     * Tries to find a web socket component by its ID
     * and returns a referenced class or null if can't find a component declaration.
     *
     * @param componentId
     * @throws AssertionError if a found component declaration has incorrect class name
     */
    private Class<?> loadWebSocketComponentClass(String componentId)
    {
        /** TODO
        IConfigurationElement componentDeclaration = findDeclaration( componentId, "com.developmentontheedge.be5.websocketcomponent", "component" );

        if( componentDeclaration == null )
            return null;

        return loadClass( componentId, componentDeclaration );
        */
        
        return null;
    }

    ///////////////////////////////////////////////////////////////////
    // misc
    //
    
    /**
     * Tries to find a component by its ID
     * and returns a referenced class or null if can't find a component declaration.
     *
     * @param componentId
     * @throws AssertionError if a found component declaration has incorrect class name
     */
    private Class<?> loadComponentClass(String componentId)
    {
        /* TODO
        IConfigurationElement componentDeclaration = findDeclaration( componentId, "com.developmentontheedge.be5.component", "component" );

        if( componentDeclaration == null )
            return null;

        return loadClass( componentId, componentDeclaration );
        */
        
        return null;
    }


    /**
     * Transforms a parameters from a multimap to a simple map,
     * ignoring parameters with several values.
     */
    private Map<String, String> simplify(Map<String, String[]> parameters)
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
