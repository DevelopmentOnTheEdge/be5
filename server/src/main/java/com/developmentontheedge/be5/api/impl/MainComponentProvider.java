package com.developmentontheedge.be5.api.impl;

import com.developmentontheedge.be5.api.ComponentProvider;
import com.developmentontheedge.be5.api.exceptions.Be5Exception;
import com.developmentontheedge.be5.components.Login;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class MainComponentProvider implements ComponentProvider
{
    private static final Logger log = Logger.getLogger(MainComponentProvider.class.getName());

    private Map<String, Class<?>> loadedClasses = new ConcurrentHashMap<>();

    @Override
    public Class<?> get(String componentId)
    {
        if(!loadedClasses.containsKey(componentId)){
            if("login".equals(componentId)){
                throw Be5Exception.unknownComponent("Component 'login' is not specified in 'config.yaml'. " +
                        "You can specify the default be5 implementation: " + Login.class.getName());
            }else{
                throw Be5Exception.unknownComponent( componentId );
            }
        }
        return loadedClasses.get(componentId);
    }

    @Override
    public void put(String componentId, Class<?> value)
    {
        if(loadedClasses.containsKey(componentId)){
            throw Be5Exception.invalidState("Component redefine forbidden.");
        }
        loadedClasses.put(componentId, value);
    }

}
