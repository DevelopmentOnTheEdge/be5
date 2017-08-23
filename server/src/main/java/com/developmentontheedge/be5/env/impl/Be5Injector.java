package com.developmentontheedge.be5.env.impl;

import static com.google.common.base.Preconditions.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import com.developmentontheedge.be5.api.Component;
import com.developmentontheedge.be5.api.exceptions.Be5Exception;
import com.developmentontheedge.be5.env.Binder;
import com.developmentontheedge.be5.api.Configurable;
import com.developmentontheedge.be5.env.Injector;
import com.developmentontheedge.be5.metadata.util.JULLogger;
import com.google.gson.Gson;

public class Be5Injector implements Injector
{
    private static final Logger log = Logger.getLogger(Be5Injector.class.getName());

    private Map<String, Class<?>> loadedClasses = new ConcurrentHashMap<>();
    private final Map<Class<?>, Class<?>> bindings = new HashMap<>();
    private final Map<Class<?>, Object> configurations = new HashMap<>();

    private final ClassToInstanceMap instantiatedServices = new ClassToInstanceMap();

    public Be5Injector(Binder binder)
    {
        binder.configure(loadedClasses, bindings, configurations);
        getLogger();
        log.info(JULLogger.infoBlock("Services initialized: " + binder.getClass().getName()));
    }

    @Override
    public <T> T get(Class<T> serviceClass)
    {
        return get(serviceClass, new ArrayList<>());
    }

    //todo EgissoLogin egissoLogin = injector.get(EgissoLogin.class); - load component by class?
    //need refactoring move google Guice - add Module with methods: bind()

    private <T> T get(Class<T> serviceClass, List<Class<?>> stack)
    {
        // It is possible to inject service provider too.
        if (serviceClass == Injector.class)
        {
            @SuppressWarnings("unchecked")
            T thiz = (T) this;
            return thiz;
        }
        
        T service = instantiatedServices.get(serviceClass);
        
        if (service == null)
        {
            service = resolveService(serviceClass, stack);
        }
        
        return service;
    }

    /**
     * Resolve a service. Adds this service to the instantiated services.
     */
    private synchronized <T> T resolveService(Class<T> serviceClass, List<Class<?>> stack)
    {
        checkState(!stack.contains(serviceClass), "Cyclic service dependency: " + stack.toString() + ", " + serviceClass.toString());
        stack.add(serviceClass);
        
        T service;
        
        try
        {
            service = instantiate(bindings.get(serviceClass), stack);
            configureIfConfigurable(service, configurations);
        }
        catch (IllegalStateException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
        {
            throw new RuntimeException("Can't instantiate or initialize " + serviceClass.getSimpleName() + " service", e);
        }
        
        stack.remove(serviceClass);
        instantiatedServices.put(serviceClass, service);
        
        return service;
    }
    
    private <T> T instantiate(Class<?> implementationClass, List<Class<?>> stack) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException
    {
        checkState(implementationClass != null);
        Constructor<?>[] constructors = implementationClass.getConstructors();
        checkState(constructors.length == 1 || constructors.length == 0);
        
        if (constructors.length == 0)
        {
            @SuppressWarnings("unchecked")
            T serviceImplementation = (T) implementationClass.newInstance();
            return serviceImplementation;
        }
        
        @SuppressWarnings("unchecked") // safe
        Constructor<T> constructor = (Constructor<T>) constructors[0];
        Class<?>[] parameterTypes = constructor.getParameterTypes();
        List<Object> resolvedParameters = new ArrayList<>();
        
        for (Class<?> parameterType : parameterTypes)
        {
            resolvedParameters.add(get(parameterType, stack));
        }
        
        return constructor.newInstance(resolvedParameters.toArray());
    }

    /**
     * Returns a created component.
     */
    @Override
    public Component getComponent(String componentId)
    {
        try
        {
            Class<?> klass = getComponentClass(componentId);
            Component component = (Component) klass.newInstance();
            configureIfConfigurable(component, configurations);
            return component;
        }
        catch( InstantiationException | IllegalAccessException | ClassCastException e )
        {
            throw Be5Exception.internal(e, "Can't create component");
        }
    }

    private Class<?> getComponentClass(String componentId)
    {
        if(!loadedClasses.containsKey(componentId)){
            if("login".equals(componentId) || "logout".equals(componentId)){
                throw Be5Exception.unknownComponent("Component '"+componentId+"' is not specified in 'context.yaml'. " +
                        "You can specify the default implementation, for example: be5/modules/core/src/test/resources/context.yaml.");
            }else{
                throw Be5Exception.unknownComponent( componentId );
            }
        }
        return loadedClasses.get(componentId);
    }

    private <T> void configureIfConfigurable(T object, Map<Class<?>, Object> configurations)
    {
        if (object instanceof Configurable)
        {
            @SuppressWarnings("unchecked")
            Configurable<Object> configurable = (Configurable<Object>) object;
            Object config = getConfiguration(object.getClass(), configurable.getConfigurationClass(), configurations);

            if(config != null)
            {
                configurable.configure(config);
            }else{
                log.warning("Module '" + object.getClass().getName() + "' not configured.");
            }
        }
    }

    private Object getConfiguration(Class<?> klass, Class<Object> configClass, Map<Class<?>, Object> configurations)
    {
        Object config = configurations.get(klass);

        if(config == null)
        {
            return null;
        }

        String componentConfigJson = new Gson().toJson(config);

        return new Gson().fromJson(componentConfigJson, configClass);
    }

}
