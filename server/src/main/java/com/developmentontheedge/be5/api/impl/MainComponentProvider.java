package com.developmentontheedge.be5.api.impl;

import com.developmentontheedge.be5.api.ComponentProvider;
import com.developmentontheedge.be5.api.exceptions.Be5Exception;

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
            if("login".equals(componentId) || "logout".equals(componentId)){
                throw Be5Exception.unknownComponent("Component 'login' is not specified in 'context.yaml'. " +
                        "You can specify the default implementation, for example: be5/modules/core/src/test/resources/context.yaml.");
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
