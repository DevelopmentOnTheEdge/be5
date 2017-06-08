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
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.logging.Logger;

public class YamlBinder implements Binder
{
    private static final Logger log = Logger.getLogger(YamlBinder.class.getName());

    @Override
    public void configure(Map<String, Class<?>> loadedClasses, Map<Class<?>, Class<?>> bindings, Map<Class<?>, Consumer<Object>> initializers, Map<String, Object> configurations)
    {
        try{
            ArrayList<URL> urls = Collections.list(getClass().getClassLoader().getResources(CONTEXT_FILE));
            for (URL url: urls)
            {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream(), "utf-8")))
                {
                    loadModules(reader, bindings, loadedClasses, initializers);
                    //loadModuleConfiguration(reader, configurations);
                }
            }
        }
        catch (IOException e)
        {
            throw Be5Exception.internal(e, "Can't load server modules.");
        }

    }


    private static final String CONTEXT_FILE = "context.yaml";

    @SuppressWarnings("unchecked")
    void loadModules(Reader reader, Map<Class<?>, Class<?>> bindings, Map<String, Class<?>> loadedClasses, Map<Class<?>, Consumer<Object>> initializers)
    {
        Map<String, Object> module = (Map<String, Object>) ((Map<String, Object>) new Yaml().load(reader)).get("context");

        List<Map<String, String>> components = ( List<Map<String, String>> ) module.get("components");
        List<Map<String, Map<String, String>>> services = ( List<Map<String, Map<String, String>>> ) module.get("services");

        if(components != null)loadComponents(loadedClasses, components);
        if(services != null)bindServices(bindings, services, initializers);
        //runInitializers( )?;

    }

    @SuppressWarnings("unchecked")
    private void loadComponents(Map<String, Class<?>> loadedClasses, List<Map<String, String>> components)
    {
        for (Map<String, String> element: components)
        {
            Map.Entry<String,String> entry = element.entrySet().iterator().next();
            Class<Object> serviceInterface = (Class<Object>) loadClass(entry.getValue());

            loadedClasses.put(entry.getKey(), serviceInterface);
        }
    }

    @SuppressWarnings("unchecked")
    private void bindServices(Map<Class<?>, Class<?>> bindings, List<Map<String, Map<String, String>>> services, Map<Class<?>, Consumer<Object>> initializers)
    {
        for (Map<String, Map<String, String>> element: services)
        {
            Map.Entry<String,Map<String, String>> entry = element.entrySet().iterator().next();
            String key = entry.getKey();
            Map<String, String> elementOptions = entry.getValue();

            Class<Object> serviceInterface = (Class<Object>) loadClass(elementOptions.get("interface"));
            Class<Object> serviceImplementation = (Class<Object>) loadClass(elementOptions.get("implementation"));

//            bindings.bind( serviceInterface, serviceImplementation, service ->
//                    configureServiceIfConfigurable(service, key)
//            );

            bindings.put(serviceInterface, serviceImplementation);
            //initializers.put(serviceInterface, service -> configureServiceIfConfigurable(service, key));
        }
    }


    private Class<?> loadClass(String path){
        try
        {
            return Class.forName(path);//Injector.class.getClassLoader().loadClass(path);
        }
        catch (ClassNotFoundException e)
        {
            throw Be5ErrorCode.INTERNAL_ERROR.rethrow(log, e,
                    "ClassNotFoundException by path='"+path+"' in " + CONTEXT_FILE);
        }
    }




    @SuppressWarnings("unchecked")
    void loadModuleConfiguration(BufferedReader reader, Map<String, Object> configurations)
    {
        Map<String, Object> moduleConfiguration = (Map<String, Object>) ((Map<String, Object>) new Yaml().load(reader)).get("config");

        if(moduleConfiguration != null) configurations.putAll(moduleConfiguration);
    }

}
