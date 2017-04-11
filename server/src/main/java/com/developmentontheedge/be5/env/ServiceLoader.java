package com.developmentontheedge.be5.env;

import com.developmentontheedge.be5.api.Component;
import com.developmentontheedge.be5.api.ComponentProvider;
import com.developmentontheedge.be5.api.Configurable;
import com.developmentontheedge.be5.api.ServiceProvider;
import com.developmentontheedge.be5.api.exceptions.impl.Be5ErrorCode;
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

public class ServiceLoader
{
    private static final Logger log = Logger.getLogger(ServiceLoader.class.getName());

    public void load(ServiceProvider serviceProvider, ComponentProvider loadedClasses) throws IOException
    {
        ArrayList<URL> urls = Collections.list((ServiceLoader.class).getClassLoader().getResources("context.yaml"));

        for (URL url: urls){
            BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream(), "utf-8"));

            loadModule(reader, serviceProvider, loadedClasses);
        }

        serviceProvider.freeze();

        log.info("Services initialized");
    }

    @SuppressWarnings("unchecked")
    void loadModule(Reader reader, ServiceProvider serviceProvider, ComponentProvider loadedClasses)
    {
        Map<String, Object> module = (Map<String, Object>) ((Map<String, Object>) new Yaml().load(reader)).get("context");

        List<Map<String, Object>> components = ( List<Map<String, Object>> ) module.get("components");
        List<Map<String, Map<String, String>>> services = ( List<Map<String, Map<String, String>>> ) module.get("services");

        if(components != null)loadComponents(loadedClasses, components);
        if(services != null)bindServices(serviceProvider, services);
        //runInitializers( );

    }

    @SuppressWarnings("unchecked")
    private void loadComponents(ComponentProvider loadedClasses, List<Map<String, Object>> components)
    {
        for (Map<String, Object> element: components)
        {
            Map.Entry<String,Object> entry = element.entrySet().iterator().next();
            Class<Object> serviceInterface;
            if(entry.getValue() instanceof String)
            {
                serviceInterface = (Class<Object>) loadClass((String)entry.getValue());
            }
            else
            {
                Map<String, Object> componentWithOptions = (Map<String, Object>) entry.getValue();
                serviceInterface = (Class<Object>) loadClass((String) componentWithOptions.get("class"));
                Integer version = (Integer) componentWithOptions.get("version");
            }

            loadedClasses.put(entry.getKey(), serviceInterface);
        }
    }

    @SuppressWarnings("unchecked")
    private void bindServices(ServiceProvider serviceProvider, List<Map<String, Map<String, String>>> services)
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

    public void configureComponentIfConfigurable(Component component, String componentId)
    {
        configureIfConfigurable(component, "components", componentId);
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

    private Class loadClass(String path){
        try
        {
            return ServiceLoader.class.getClassLoader().loadClass(path);
        }
        catch (ClassNotFoundException e)
        {
            throw Be5ErrorCode.INTERNAL_ERROR.rethrow(log, e);
        }
    }
}
