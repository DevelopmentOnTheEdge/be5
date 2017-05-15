package com.developmentontheedge.be5.api.impl;

import static com.google.common.base.Preconditions.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import com.developmentontheedge.be5.api.ServiceProvider;
import com.developmentontheedge.be5.util.ClassToInstanceMap;

public class MainServiceProvider implements ServiceProvider
{
    
    private final Map<Class<?>, Class<?>> bindings = new HashMap<>();
    private final Map<Class<?>, Consumer<Object>> initializers = new HashMap<>();
    private final ClassToInstanceMap instantiatedServices = new ClassToInstanceMap();
    private boolean frozen = false;
    
    public MainServiceProvider()
    {
        // do nothing
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public <T, TT extends T> void bind(Class<T> serviceClass, Class<TT> implementationClass, Consumer<TT> initializer)
    {
        if (frozen)
        {
            throw new IllegalStateException("Too late");
        }
        
        bindings.put(serviceClass, implementationClass);
        initializers.put(serviceClass, (Consumer<Object>) initializer);
    }
    
    @Override
    public void freeze()
    {
        this.frozen = true;
    }

    @Override
    public <T> T get(Class<T> serviceClass)
    {
        return get(serviceClass, new ArrayList<>());
    }
    
    private <T> T get(Class<T> serviceClass, List<Class<?>> stack)
    {
        // It is possible to inject service provider too.
        if (serviceClass == ServiceProvider.class)
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
            initializers.get(serviceClass).accept(service);
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

}
