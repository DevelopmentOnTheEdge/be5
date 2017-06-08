package com.developmentontheedge.be5.env.impl;

import com.developmentontheedge.be5.api.Component;
import com.developmentontheedge.be5.api.Configurable;
import com.developmentontheedge.be5.api.exceptions.Be5ErrorCode;
import com.google.gson.Gson;

import java.util.Map;
import java.util.logging.Logger;

class Configurator
{
    private static final Logger log = Logger.getLogger(Configurator.class.getName());

    static void configureComponentIfConfigurable(Component component, String componentId, Map<String, Object> configurations)
    {
        configureIfConfigurable(component, "components", componentId, configurations);
    }

    static void configureServiceIfConfigurable(Object service, String serviceId, Map<String, Object> configurations)
    {
        configureIfConfigurable(service, "services", serviceId, configurations);
    }

    private static <T> void configureIfConfigurable(T object, String collection, String id, Map<String, Object> configurations)
    {
        if (object instanceof Configurable)
        {
            @SuppressWarnings("unchecked")
            Configurable<Object> configurable = (Configurable<Object>) object;
            Object config = getConfiguration(configurable.getConfigurationClass(), collection, id, configurations);

            configurable.configure(config);
        }
    }

    private static <T> T getConfiguration(Class<T> configClass, String collection, String id, Map<String, Object> configurations)
    {
        if (configurations == null)
        {
            throw Be5ErrorCode.INTERNAL_ERROR.exception("Call loadConfiguration first.");
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> config = (Map<String, Object>) configurations.get(collection);

        if(config == null){
            log.warning("Module in " + collection + " '" + id + "' not configured.");
            return null;
        }

        Object configObject = config.get(id);

        if (configObject == null)
        {
            try
            {
                return configClass.newInstance();
            }
            catch (InstantiationException | IllegalAccessException e)
            {
                throw new RuntimeException();
            }
        }

        String componentConfigJson = new Gson().toJson(configObject);

        return new Gson().fromJson(componentConfigJson, configClass);
    }
}
