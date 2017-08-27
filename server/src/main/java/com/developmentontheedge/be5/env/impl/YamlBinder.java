package com.developmentontheedge.be5.env.impl;

import com.developmentontheedge.be5.api.exceptions.Be5ErrorCode;
import com.developmentontheedge.be5.api.exceptions.Be5Exception;
import com.developmentontheedge.be5.env.Binder;
import org.yaml.snakeyaml.Yaml;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class YamlBinder implements Binder
{
    private static final Logger log = Logger.getLogger(YamlBinder.class.getName());

    static final String CONTEXT_FILE = "context.yaml";
    private final Map<String, Class<?>> serviceKeys = new HashMap<>();

    @Override
    public void configure(Map<String, Class<?>> loadedClasses, Map<Class<?>, Class<?>> bindings,
                          Map<Class<?>, Object> configurations)
    {
        try{
            ArrayList<URL> urls = Collections.list(getClass().getClassLoader().getResources(CONTEXT_FILE));

            for (URL url: urls)
            {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream(), "utf-8"))) {
                    loadModules(reader, bindings, loadedClasses);
                }
            }

            for (URL url: urls)
            {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream(), "utf-8")))
                {
                    loadModuleConfiguration(reader, configurations);
                }
            }
        }
        catch (IOException e)
        {
            throw Be5Exception.internal(e, "Can't load server modules.");
        }

    }

    @SuppressWarnings("unchecked")
    void loadModules(Reader reader, Map<Class<?>, Class<?>> bindings, Map<String, Class<?>> loadedClasses)
    {
        Map<String, Object> moduleContext = (Map<String, Object>) ((Map<String, Object>) new Yaml().load(reader)).get("context");
        if(moduleContext != null)
        {
            List<Map<String, String>> components = (List<Map<String, String>>) moduleContext.get("components");
            List<Object> services = (List<Object>) moduleContext.get("services");

            if (components != null) loadComponents(loadedClasses, components);
            if (services != null) bindServices(bindings, services);
        }
    }

    @SuppressWarnings("unchecked")
    private void loadComponents(Map<String, Class<?>> loadedClasses, List<Map<String, String>> components)
    {
        for (Map<String, String> element: components)
        {
            Map.Entry<String,String> entry = element.entrySet().iterator().next();
            Class<Object> serviceInterface = (Class<Object>) loadClass(entry.getValue());

            if(loadedClasses.containsKey(entry.getKey()))
            {
                throw Be5Exception.internal("Redefining in yaml config not allowed.");
            }

            loadedClasses.put(entry.getKey(), serviceInterface);
        }
    }

    @SuppressWarnings("unchecked")
    private void bindServices(Map<Class<?>, Class<?>> bindings, List<Object> services)
    {
        for (Object element: services)
        {
            String key;
            Class<Object> serviceKeyClass, serviceImplementation;

            if(element instanceof Map)
            {
                Map.Entry<String, Map<String, String>> entry = ((Map<String, Map<String, String>>)element).entrySet().iterator().next();
                key = entry.getKey();
                Map<String, String> elementOptions = entry.getValue();

                serviceKeyClass = (Class<Object>) loadClass(elementOptions.get("interface"));
                serviceImplementation = (Class<Object>) loadClass(elementOptions.get("implementation"));

                if (serviceKeys.containsKey(key))
                {
                    throw Be5Exception.internal("Equals key not allowed.");
                }
            }
            else
            {
                key = (String) element;
                serviceKeyClass = (Class<Object>) loadClass(key);
                serviceImplementation = serviceKeyClass;
            }

            if (bindings.containsKey(serviceKeyClass))
            {
                throw Be5Exception.internal("Redefining in yaml config not allowed.");
            }

            bindings.put(serviceKeyClass, serviceImplementation);
            serviceKeys.put(key, serviceKeyClass);
        }
    }

    @SuppressWarnings("unchecked")
    void loadModuleConfiguration(BufferedReader reader, Map<Class<?>, Object> configurations)
    {
        Object configObject = ((Map<String, Object>) new Yaml().load(reader)).get("config");
        if(configObject != null)
        {
            Map<String, Object> config = (Map<String, Object>) configObject;

            for (Map.Entry<String, Object> entry : config.entrySet()){
                configurations.put(loadClass(entry.getKey()), entry.getValue());
            }

        }
    }

    private Class<?> loadClass(String path){
        try
        {
            return Class.forName(path);
        }
        catch (ClassNotFoundException e)
        {
            throw Be5ErrorCode.INTERNAL_ERROR.rethrow(log, e,
                    "ClassNotFoundException by path='"+path+"' in " + CONTEXT_FILE);
        }
    }

}
