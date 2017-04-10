package com.developmentontheedge.be5.servlet;

import com.developmentontheedge.be5.api.Component;
import com.developmentontheedge.be5.api.Configurable;
import com.developmentontheedge.be5.api.ServiceProvider;
import com.developmentontheedge.be5.api.exceptions.impl.Be5ErrorCode;
import com.developmentontheedge.be5.env.ConfigurationProvider;
import org.yaml.snakeyaml.Yaml;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class ServiceLoader
{
    private static final Logger log = Logger.getLogger(ServiceLoader.class.getName());

    public void load(ServiceProvider serviceProvider, Map<String, Class<?>> loadedClasses) throws IOException
    {
        ArrayList<URL> urls = Collections.list((MainServlet.class).getClassLoader().getResources("context.yaml"));
        for (URL url: urls){
            BufferedReader r = new BufferedReader(new InputStreamReader(url.openStream(), "utf-8"));

            @SuppressWarnings( "unchecked" )
            Map<String, Object> module = ( Map<String, Object> ) new Yaml().load( r );
            @SuppressWarnings( "unchecked" )
            List<Object> components = ( List<Object> ) module.get("components");
            @SuppressWarnings( "unchecked" )
            List<Object> services = ( List<Object> ) module.get("services");


            for (Object componentObj: components)
            {
                @SuppressWarnings( "unchecked" )
                Map<String, String> element = (Map<String, String>) componentObj;

                Map.Entry<String,String> entry= element.entrySet().iterator().next();

                @SuppressWarnings("unchecked")
                Class<Object> serviceInterface = (Class<Object>) loadClass(entry.getValue());

                loadedClasses.put(entry.getKey(), serviceInterface);
            }

            for (Object serviceObj: services)
            {
                @SuppressWarnings( "unchecked" )
                Map<String, Map<String, String>> element = (Map<String, Map<String, String>>) serviceObj;

                Map.Entry<String,Map<String, String>> entry= element.entrySet().iterator().next();
                String key = entry.getKey();
                Map<String, String> elementOptions = entry.getValue();

                @SuppressWarnings("unchecked")
                Class<Object> serviceInterface = (Class<Object>) loadClass(elementOptions.get("interface"));
                @SuppressWarnings("unchecked")
                Class<Object> serviceImplementation = (Class<Object>) loadClass(elementOptions.get("implementation"));

                serviceProvider.bind( serviceInterface, serviceImplementation, service ->
                        configureServiceIfConfigurable(service, key)
                );
            }


        }

        serviceProvider.freeze();

        log.info("Services initialized");
    }

    private static void configureServiceIfConfigurable(Object service, String serviceId)
    {
        configureIfConfigurable(service, "services", serviceId);
    }

    protected static void configureComponentIfConfigurable(Component component, String componentId)
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
        } catch (ClassNotFoundException e)
        {
            throw Be5ErrorCode.INTERNAL_ERROR.rethrow(log, e);
        }
    }
}
