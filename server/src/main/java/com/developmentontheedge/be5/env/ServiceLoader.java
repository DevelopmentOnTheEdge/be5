package com.developmentontheedge.be5.env;

import com.developmentontheedge.be5.api.Component;
import com.developmentontheedge.be5.api.Configurable;
import com.developmentontheedge.be5.api.ServiceProvider;
import com.developmentontheedge.be5.api.exceptions.impl.Be5ErrorCode;
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

    @SuppressWarnings("unchecked")
    public void load(ServiceProvider serviceProvider, Map<String, Class<?>> loadedClasses) throws IOException
    {
        ArrayList<URL> urls = Collections.list((ServiceLoader.class).getClassLoader().getResources("context.yaml"));

        for (URL url: urls){
            BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream(), "utf-8"));

            Map<String, Object> module = (Map<String, Object>) ((Map<String, Object>) new Yaml().load(reader)).get("context");

            List<Map<String, String>> components = ( List<Map<String, String>> ) module.get("components");
            List<Map<String, Map<String, String>>> services = ( List<Map<String, Map<String, String>>> ) module.get("services");

            for (Map<String, String> element: components)
            {
                Map.Entry<String,String> entry = element.entrySet().iterator().next();

                Class<Object> serviceInterface = (Class<Object>) loadClass(entry.getValue());

                loadedClasses.put(entry.getKey(), serviceInterface);
            }

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

        serviceProvider.freeze();

        log.info("Services initialized");
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
        } catch (ClassNotFoundException e)
        {
            throw Be5ErrorCode.INTERNAL_ERROR.rethrow(log, e);
        }
    }
}
