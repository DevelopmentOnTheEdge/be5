package com.developmentontheedge.be5.servlet;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.websocket.CloseReason;
import javax.websocket.CloseReason.CloseCodes;
import javax.websocket.Session;

import com.developmentontheedge.be5.api.helpers.UserInfo;
import com.developmentontheedge.be5.metadata.Utils;
import com.developmentontheedge.be5.env.ConfigurationProvider;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;

//import com.developmentontheedge.be5.DaemonStarter;
import com.developmentontheedge.dbms.DbmsConnector;

import com.developmentontheedge.be5.api.Component;
import com.developmentontheedge.be5.api.Configurable;
import com.developmentontheedge.be5.api.Initializer;
import com.developmentontheedge.be5.api.InitializerContext;
import com.developmentontheedge.be5.api.Request;
import com.developmentontheedge.be5.api.WebSocketComponent;
import com.developmentontheedge.be5.api.exceptions.Be5Exception;
import com.developmentontheedge.be5.api.exceptions.impl.Be5ErrorCode;
import com.developmentontheedge.be5.api.helpers.UserInfoManager;
import com.developmentontheedge.be5.api.impl.InitializerContextImpl;
import com.developmentontheedge.be5.api.impl.MainServiceProvider;
import com.developmentontheedge.be5.api.impl.RequestImpl;
import com.developmentontheedge.be5.api.impl.ResponseImpl;
import com.developmentontheedge.be5.api.impl.WebSocketContextImpl;
import com.developmentontheedge.be5.api.services.Logger;
import com.developmentontheedge.be5.env.Be5ClassLoader;
import com.developmentontheedge.be5.env.Classes;
import com.developmentontheedge.be5.util.Delegator;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;

/**
 * The main router.
 * 
 * @author asko
 */
public class MainServletImpl
{

    /**
     * Classes cache: componentId->class.
     */
    private static final Map<String, Class<?>> loadedClasses = new ConcurrentHashMap<>();

    /**
     * Classes cache: webSocketComponentId->class.
     */
    private static final Map<String, Class<?>> loadedWsClasses = new ConcurrentHashMap<>();

    //TODO private final DaemonStarter starter;
    private final MainServiceProvider serviceProvider;

    public MainServletImpl()
    {
        //this.starter = new DaemonStarter();
        this.serviceProvider = new MainServiceProvider();
    }

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

    public void init(Object config)
    {
        ServletConfig cfg = Delegator.on( config, ServletConfig.class );
        Utils.setClassLoader( new Be5ClassLoader() );
//        Utils.setDefaultConnector( Be5.getDbmsConnector() );
//        try
//        {
//TODO            starter.init( new LegacyServletConfig( "DaemonStarter" ) );
//        }
//        catch( ServletException e )
//        {
//            throw Be5Exception.internal( e );
//        }
        bindServices(cfg.getServletContext());
        runInitializers( cfg );
    }

    private void bindServices(ServletContext servletContext)
    {
        for( IConfigurationElement element : getConfigurationElements( "com.developmentontheedge.be5.service" ) )
        {
            if( element.getName().equals( "service" ) )
            {
                bindService( element );
            }
        }
        serviceProvider.bind(Logger.class, ServletLogger.class, s -> s.setContext(servletContext));

        serviceProvider.freeze();

        serviceProvider.getLogger().info("Services initialized");
    }

    private void bindService(IConfigurationElement element)
    {
        String serviceClassName = element.getAttribute("interface");
        String implementationClassName = element.getAttribute("implementation");
        String bundleName = element.getContributor().getName();
        Bundle bundle = Platform.getBundle(bundleName);
        String id = element.getAttribute("id");

        try
        {
            @SuppressWarnings("unchecked")
            Class<Object> serviceClass = (Class<Object>) bundle.loadClass(serviceClassName);
            @SuppressWarnings("unchecked")
            Class<Object> implementationClass = (Class<Object>) bundle.loadClass(implementationClassName);
            Consumer<Object> initializer = service -> { /* do nothing */ };

            if (id != null)
            {
                initializer = service -> configureServiceIfConfigurable(service, id);
            }

            serviceProvider.bind(serviceClass, implementationClass, initializer);
        }
        catch (ClassNotFoundException e)
        {
            serviceProvider.getLogger().error(e);
            throw new AssertionError( "Can't find a service class " + serviceClassName + "/" + implementationClassName, e );
        }
        catch (RuntimeException e)
        {
            serviceProvider.getLogger().error(e);
            throw e;
        }
    }

    private void runInitializers(ServletConfig config)
    {

        InitializerContext context = new InitializerContextImpl( config );

        for( IConfigurationElement element : getConfigurationElements( "com.developmentontheedge.be5.initializer" ) )
        {
            if( element.getName().equals( "initializer" ) )
            {
                runInitializer( element, context );
            }
        }
    }

    private void runInitializer(IConfigurationElement element, InitializerContext context)
    {
        try
        {
            String id = element.getAttribute( "id" );
            Initializer initializer = (Initializer) loadClass( id, element ).newInstance();

            configureInitializerIfConfigurable(initializer, id);
            initializer.initialize( context, serviceProvider );
        }
        catch( Throwable e )
        {
            serviceProvider.getLogger().error(e);
            throw Be5Exception.internal( e );
        }
    }

//    public void destroy()
//    {
//        starter.destroy();
//    }

    /**
     * The general routing method. Tries to determine and find a component using a given request URI.
     * Generation of response is delegated to a found component.
     *
     * @throws RuntimeException
     */
    public void respond(Object requestObj, Object responseObj, String method, String requestUri, Map<String, String[]> parameters)
    {
        Pattern uriPattern = Pattern.compile( "/.*?/api/(.*)" );
        Matcher matcher = uriPattern.matcher( requestUri );
        HttpServletRequest request = Delegator.on( requestObj, HttpServletRequest.class );
        HttpServletResponse response = Delegator.on( responseObj, HttpServletResponse.class );

        String origin = request.getHeader( "Origin" );

        // TODO test origin

        response.addHeader( "Access-Control-Allow-Credentials", "true" );
        response.addHeader( "Access-Control-Allow-Origin", origin );
        response.addHeader( "Access-Control-Allow-Methods", "POST, GET" );
        response.addHeader( "Access-Control-Max-Age", "1728000" );

        if( !matcher.matches() )
        {
            trySendError( HttpServletResponse.SC_NOT_FOUND, response );
            return;
        }

        String[] uriParts = matcher.group( 1 ).split( "/" ); // excluding '<projectName>/api/'
        String componentId = uriParts[0];

        Component component;

        try
        {
            component = createComponent( componentId );
        }
        catch( Be5Exception e )
        {
            serviceProvider.getLogger().error(e);
            trySendError( HttpServletResponse.SC_INTERNAL_SERVER_ERROR, response );
            return;
        }

        String subRequestUri = Joiner.on( '/' ).join( Iterables.skip( Arrays.asList( uriParts ), 1 ) );
        Request req = new RequestImpl( request, subRequestUri, simplify( parameters ) );

        // do some preprocessing using
        // a registered ('system -> REQUEST_PREPROCESSOR') request preprocessor
        try
        {
            preprocessRequest( request, serviceProvider.getDbmsConnector(), UserInfoManager.get(req, serviceProvider).getUserInfo(), "qps" );
        }
        catch( Exception e ) // ignore checkers' warnings, we want to catch them all
        {
            serviceProvider.getLogger().error(e);
            trySendError( HttpServletResponse.SC_INTERNAL_SERVER_ERROR, response );
            return;
        }

        try
        {
            configureComponentIfConfigurable(component, componentId);
            component.generate( req, new ResponseImpl(response), serviceProvider );
        }
        catch ( RuntimeException | Error e )
        {
            serviceProvider.getLogger().error(e);
            throw e;
        }
    }

    void preprocessRequest(HttpServletRequest request, DbmsConnector connector, UserInfo userInfo, String url)
            throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException
    {
        String className = Utils.getSystemSetting( connector, "REQUEST_PREPROCESSOR" );

        if( className != null )
        {
            RequestPreprocessor preprocessor = Classes.tryLoad( className, RequestPreprocessor.class )
                    .getConstructor( DbmsConnector.class, UserInfo.class ).newInstance( connector, userInfo );

            preprocessor.preprocessUrl( request, url );
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
            serviceProvider.getLogger().error(e);
        }
    }

    /**
     * Returns a created component.
     */
    private Component createComponent(String componentId)
    {
        try
        {
            Class<?> klass = loadedClasses.computeIfAbsent( componentId, this::loadComponentClass );
            if( klass == null )
                throw Be5ErrorCode.UNKNOWN_COMPONENT.exception( componentId );
            return (Component)klass.newInstance();
        }
        catch( InstantiationException | IllegalAccessException | ClassCastException e )
        {
            throw Be5Exception.internal( e );
        }
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
     * Tries to find a component by its ID
     * and returns a referenced class or null if can't find a component declaration.
     *
     * @param componentId
     * @throws AssertionError if a found component declaration has incorrect class name
     */
    private Class<?> loadComponentClass(String componentId)
    {
        IConfigurationElement componentDeclaration = findDeclaration( componentId, "com.developmentontheedge.be5.component", "component" );

        if( componentDeclaration == null )
            return null;

        return loadClass( componentId, componentDeclaration );
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
        IConfigurationElement componentDeclaration = findDeclaration( componentId, "com.developmentontheedge.be5.websocketcomponent", "component" );

        if( componentDeclaration == null )
            return null;

        return loadClass( componentId, componentDeclaration );
    }

    /**
     * Tries to load a class by its name from the configuration element.
     */
    private Class<?> loadClass(String id, IConfigurationElement configuration) throws AssertionError
    {
        String className = configuration.getAttribute( "class" );
        String bundleName = configuration.getContributor().getName();
        Bundle bundle = Platform.getBundle( bundleName );

        if( bundle == null )
            throw new AssertionError( "Can't find a bundle '" + bundleName + "'" );

        try
        {
            return bundle.loadClass( className );
        }
        catch( ClassNotFoundException e )
        {
            throw new AssertionError( "Can't find a class '" + className + "' in bundle '" + bundleName + "' decalared in '" + id + "'", e );
        }
    }

    /**
     * Tries to find an extension with a given ID.
     */
    private IConfigurationElement findDeclaration(String componentId, String extensionPoint, String extensionTag)
    {
        IConfigurationElement[] elements = getConfigurationElements( extensionPoint );
        IConfigurationElement defaultElement = null;

        for( IConfigurationElement element : elements )
        {
            String tag = element.getName();
            if( tag.equals( extensionTag ) )
            {
                String id = element.getAttribute( "id" );
                boolean isDefault = "true".equals(element.getAttribute( "default" ));
                // id == null => incorrect declaration
                if( componentId.equals( id ) )
                {
                    if(isDefault)
                    {
                        defaultElement = element;
                    }
                    else
                    {
                        return element;
                    }
                }
            }
        }

        return defaultElement;
    }

    private IConfigurationElement[] getConfigurationElements(String extensionPoint)
    {
        IExtensionRegistry extensionRegistry = Platform.getExtensionRegistry();
        IConfigurationElement[] elements = extensionRegistry.getConfigurationElementsFor( extensionPoint );

        return elements;
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

    private static void configureComponentIfConfigurable(Component component, String componentId)
    {
        configureIfConfigurable(component, "components", componentId);
    }

    private static void configureInitializerIfConfigurable(Initializer initializer, String initializerId)
    {
        configureIfConfigurable(initializer, "initializers", initializerId);
    }

    private static void configureServiceIfConfigurable(Object service, String serviceId)
    {
        configureIfConfigurable(service, "services", serviceId);
    }

    private static <T> void configureIfConfigurable(T object, String collection, String id)
    {
        if (object instanceof Configurable)
        {
            @SuppressWarnings("unchecked")
            Configurable<Object> configurable = (Configurable<Object>) object;
            Object config = ConfigurationProvider.INSTANCE.loadConfiguration(configurable.getConfigurationClass(), collection, id);

            configurable.configure(config);
        }
    }

}
