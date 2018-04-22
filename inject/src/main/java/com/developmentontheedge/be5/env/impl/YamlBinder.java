package com.developmentontheedge.be5.env.impl;

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


public class YamlBinder implements Binder
{
    static final String CONTEXT_FILE = "context.yaml";
    private final Map<String, Class<?>> serviceKeys = new HashMap<>();

    public YamlBinder() {}

    @Override
    public String getInfo()
    {
        return "";
    }

    @Override
    public void configure(Map<String, Class<?>> loadedClasses, Map<Class<?>, Class<?>> bindings,
                          Map<Class<?>, Object> configurations, List<Class<?>> requestPreprocessors)
    {
        try{
            ArrayList<URL> urls = Collections.list(getClass().getClassLoader().getResources(CONTEXT_FILE));

            for (URL url: urls)
            {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream(), "utf-8")))
                {
                    loadModules(reader, bindings, loadedClasses, configurations, requestPreprocessors);
                }
            }
        }
        catch (IOException e)
        {
            throw new RuntimeException("Can't load server modules.", e);
        }

    }

    @SuppressWarnings("unchecked")
    public boolean isServer(Reader reader)
    {
        Object name = ((Map<String, Object>) new Yaml().load(reader)).get("name");

        return "be5-server".equals(name);
    }

    @SuppressWarnings("unchecked")
    void loadModules(Reader reader, Map<Class<?>, Class<?>> bindings, Map<String, Class<?>> loadedClasses,
                     Map<Class<?>, Object> configurations, List<Class<?>> requestPreprocessors)
    {
        Map<String, Object> file = (Map<String, Object>) new Yaml().load(reader);
        Map<String, Object> moduleContext = (Map<String, Object>) file.get("context");
        if(moduleContext != null)
        {
            List<Map<String, String>> components = (List<Map<String, String>>) moduleContext.get("components");
            List<Object> services = (List<Object>) moduleContext.get("services");

            if (components != null) loadComponents(loadedClasses, components);
            if (services != null) bindServices(bindings, services);
        }

        Map<String, Object> config = (Map<String, Object>) file.get("config");
        if(config != null)
        {
            for (Map.Entry<String, Object> entry : config.entrySet())
            {
                configurations.put(loadClass(entry.getKey()), entry.getValue());
            }
        }

        List<String> requestPreprocessorsConfig = (List<String>) file.get("requestPreprocessors");
        if(requestPreprocessorsConfig != null)
        {
            for (String requestPreprocessorClassName : requestPreprocessorsConfig)
            {
                Class<?> aClass = loadClass(requestPreprocessorClassName);
                requestPreprocessors.add(aClass);
                bindings.put(aClass, aClass);
            }
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
                throw new RuntimeException("Redefining in yaml config not allowed: " + entry.getKey());
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
                    throw new RuntimeException("Equals key not allowed.");
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
                throw new RuntimeException("Redefining in yaml config not allowed: " + serviceKeyClass);
            }

            bindings.put(serviceKeyClass, serviceImplementation);
            serviceKeys.put(key, serviceKeyClass);
        }
    }

    private Class<?> loadClass(String path){
        try
        {
            return Class.forName(path);
        }
        catch (ClassNotFoundException e)
        {
            throw new RuntimeException("ClassNotFoundException by path='" + path + "' in " + CONTEXT_FILE, e);
        }
    }

}
