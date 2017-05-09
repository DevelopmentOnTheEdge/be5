package com.developmentontheedge.be5.env;

import com.developmentontheedge.be5.api.Component;
import com.developmentontheedge.be5.api.ComponentProvider;
import com.developmentontheedge.be5.api.Configurable;
import com.developmentontheedge.be5.api.ServiceProvider;
import com.developmentontheedge.be5.api.exceptions.Be5ErrorCode;
import com.developmentontheedge.be5.api.exceptions.Be5Exception;
import com.developmentontheedge.be5.api.impl.MainComponentProvider;
import com.developmentontheedge.be5.api.impl.MainServiceProvider;
import org.yaml.snakeyaml.Yaml;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class ServerModules
{
    private static final Logger log = Logger.getLogger(ServerModules.class.getName());

    private final ServiceProvider serviceProvider = new MainServiceProvider();
    private final ComponentProvider loadedClasses = new MainComponentProvider();

    /**
     * Classes cache: webSocketComponentId->class.
     */
    //private static final Map<String, Class<?>> loadedWsClasses = new ConcurrentHashMap<>();

    static final String CONTEXT_FILE = "context.yaml";

    private static class SingletonHolder {
        private static final ServerModules instance = new ServerModules();
    }

    public static ServiceProvider getServiceProvider() {
        return SingletonHolder.instance.serviceProvider;
    }

    public static <T> T getService(Class<T> serviceClass) {
        return SingletonHolder.instance.serviceProvider.get(serviceClass);
    }

    public static ComponentProvider getComponents() {
        return SingletonHolder.instance.loadedClasses;
    }

    public static Component getComponent(String componentId) {
        return SingletonHolder.instance.loadedClasses.get(componentId);
    }

    public static void configureComponentIfConfigurable(Component component, String componentId)
    {
        configureIfConfigurable(component, "components", componentId);
    }

    private ServerModules() {
        try{
            ArrayList<URL> urls = Collections.list(getClass().getClassLoader().getResources(CONTEXT_FILE));
            for (URL url: urls)
            {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream(), "utf-8")))
                {
                    loadModules(reader, serviceProvider, loadedClasses);
                }
            }
        }
        catch (IOException e)
        {
            throw Be5Exception.internal(e, "Can't load server modules.");
        }

        ConfigurationProvider.INSTANCE.loadConfiguration();

        serviceProvider.freeze();
        log.info("Services initialized");

        //config logger first
        serviceProvider.getLogger();

        if(serviceProvider.getProject().getConnectionProfile() != null)
        {
            serviceProvider.getDatabaseService();
        }
    }

    @SuppressWarnings("unchecked")
    static void loadModules(Reader reader, ServiceProvider serviceProvider, ComponentProvider loadedClasses)
    {
        Map<String, Object> module = (Map<String, Object>) ((Map<String, Object>) new Yaml().load(reader)).get("context");

        List<Map<String, String>> components = ( List<Map<String, String>> ) module.get("components");
        List<Map<String, Map<String, String>>> services = ( List<Map<String, Map<String, String>>> ) module.get("services");

        if(components != null)loadComponents(loadedClasses, components);
        if(services != null)bindServices(serviceProvider, services);
        //runInitializers( )?;

    }

    @SuppressWarnings("unchecked")
    private static void loadComponents(ComponentProvider loadedClasses, List<Map<String, String>> components)
    {
        for (Map<String, String> element: components)
        {
            Map.Entry<String,String> entry = element.entrySet().iterator().next();
            Class<Object> serviceInterface = (Class<Object>) loadClass(entry.getValue());

            loadedClasses.put(entry.getKey(), serviceInterface);
        }
    }

    @SuppressWarnings("unchecked")
    private static void bindServices(ServiceProvider serviceProvider, List<Map<String, Map<String, String>>> services)
    {
        for (Map<String, Map<String, String>> element: services)
        {
            Map.Entry<String,Map<String, String>> entry = element.entrySet().iterator().next();
            String key = entry.getKey();
            Map<String, String> elementOptions = entry.getValue();

            Class<Object> serviceInterface = (Class<Object>) loadClass(elementOptions.get("interface"));
            Class<Object> serviceImplementation = (Class<Object>) loadClass(elementOptions.get("implementation"));

            serviceProvider.bind( serviceInterface, serviceImplementation, service ->
                    configureServiceIfConfigurable(service, key)
            );
        }
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
            Object config = ConfigurationProvider.INSTANCE.getConfiguration(configurable.getConfigurationClass(), collection, id);

            configurable.configure(config);
        }
    }

    private static Class<?> loadClass(String path){
        try
        {
            return Class.forName(path);//ServerModules.class.getClassLoader().loadClass(path);
        }
        catch (ClassNotFoundException e)
        {
            throw Be5ErrorCode.INTERNAL_ERROR.rethrow(log, e,
                    "ClassNotFoundException by path='"+path+"' in " + CONTEXT_FILE);
        }
    }
}
