package com.developmentontheedge.be5.api.impl;

import com.developmentontheedge.be5.api.exceptions.impl.Be5ErrorCode;
import com.developmentontheedge.be5.components.Login;
import com.developmentontheedge.be5.env.ServiceLoader;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class ComponentProvider
{
    private static final Logger log = Logger.getLogger(ServiceLoader.class.getName());

    private Map<String, Class<?>> loadedClasses = new ConcurrentHashMap<>();

    public Class<?> get(String componentId)
    {
        if(!loadedClasses.containsKey(componentId)){
            if("login".equals(componentId)){
                throw Be5ErrorCode.STATE_INVALID.exception(log,"Component 'login' is not specified in 'config.yaml'. " +
                        "You can specify the default be5 implementation: " + Login.class.getName());
            }else{
                throw Be5ErrorCode.UNKNOWN_COMPONENT.exception(log, componentId );
            }
        }
        return loadedClasses.get(componentId);
    }

    public Class<?> put(String componentId, Class<?> value)
    {
        if(loadedClasses.containsKey(componentId)){
            throw Be5ErrorCode.STATE_INVALID.exception(log,"Component redefine forbidden.");
        }
        return loadedClasses.put(componentId, value);
    }

}
