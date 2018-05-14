package com.developmentontheedge.be5.inject.impl;

import static com.google.common.base.Preconditions.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.developmentontheedge.be5.inject.Binder;
import com.developmentontheedge.be5.inject.Inject;
import com.developmentontheedge.be5.inject.Injector;
import com.developmentontheedge.be5.inject.Stage;

import javax.inject.Provider;


public class Be5Injector implements Injector
{
    private static final Logger log = Logger.getLogger(Be5Injector.class.getName());

    private Map<String, Class<?>> loadedClasses = new HashMap<>();
    private final Map<Class<?>, Class<?>> bindings = new HashMap<>();

    private final List<Class<?>> requestPreprocessors = new ArrayList<>();

    private final Map<Class<?>, Object> configurations = new HashMap<>();

    private final ClassToInstanceMap instantiatedServices = new ClassToInstanceMap();

    private final Stage stage;

    public Be5Injector(Binder binder)
    {
        this.stage = Stage.PRODUCTION;
        init(binder);
    }

    public Be5Injector(Stage stage, Binder binder)
    {
        this.stage = stage;
        init(binder);
    }

    private void init(Binder binder)
    {
        binder.configure(loadedClasses, bindings, configurations, requestPreprocessors);

        log.info("Load classes: " + binder.getClass().getName() +
                    (!binder.getInfo().isEmpty() ? " - " + binder.getInfo() : "") + "\n" +
                 stage + " stage");

        if (stage == Stage.PRODUCTION)
        {
            List<String> serviceNames = new ArrayList<>();
            for (Class<?> service : bindings.keySet())
            {
                serviceNames.add(service.getName());
                get(service);
            }
            log.fine("Services:\n" +
                    serviceNames.stream().sorted().collect(Collectors.joining("\n")));
        }
    }

    @Override
    public <T> T get(Class<T> serviceClass)
    {
        return get(serviceClass, new ArrayList<>());
    }

    private <T> T get(Class<T> serviceClass, List<Class<?>> stack)
    {
        // It is possible to inject service provider too.
        if (serviceClass == Injector.class)
        {
            @SuppressWarnings("unchecked")
            T thiz = (T) this;
            return thiz;
        }

        if (serviceClass == Stage.class)
        {
            @SuppressWarnings("unchecked")
            T stage = (T) this.stage;
            return stage;
        }

        T service = instantiatedServices.get(serviceClass);

        if (service == null)
        {
            service = resolveServiceAndInject(serviceClass, stack);
        }

        return service;
    }

    private synchronized <T> T resolveServiceAndInject(Class<T> serviceClass, List<Class<?>> stack)
    {
        T service = resolveService(serviceClass, stack);

        injectAnnotatedFields(service);

        return service;
    }

    /**
     * Resolve a service. Adds this service to the instantiated services.
     */
    private <T> T resolveService(Class<T> serviceClass, List<Class<?>> stack)
    {
        checkState(!stack.contains(serviceClass), "Cyclic service dependency: " + stack.toString()
                .replace(", ", ",\n    ")
                .replace("[", "[\n    ")
                .replace("]", "\n]\n")+ ", " + serviceClass.toString());
        stack.add(serviceClass);
        
        T service;
        
        try
        {
            Class<?> clazz = bindings.get(serviceClass);
            if(clazz == null)
            {
                throw new RuntimeException("Class not binded " + serviceClass.getName() + ", may be has been used implimentation instead interface.");
            }
            service = instantiate(bindings.get(serviceClass), stack);
        }
        catch (IllegalStateException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
        {
            if(e instanceof InvocationTargetException)
            {
                e.getCause().printStackTrace();
            }
            throw new RuntimeException("Can't instantiate or initialize " + serviceClass.getName() + " service, " +
                    "may be constructor is not public", e);
        }
        
        stack.remove(serviceClass);


        instantiatedServices.put(serviceClass, service);
        log.finest("resolve: " + serviceClass.getName() + ", stack.size = " + stack.size());

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
        Type[] genericParameterTypes = constructor.getGenericParameterTypes();
        List<Object> resolvedParameters = new ArrayList<>();
        
        for (int i = 0 ;i < parameterTypes.length; i++)
        {
            Class<?> parameterType = parameterTypes[i];
            if(parameterType == Provider.class)
            {
                String typeName = ((ParameterizedType) genericParameterTypes[i]).getActualTypeArguments()[0].getTypeName();
                resolvedParameters.add(new ProviderImpl<>(this, loadClass(typeName)));
            }
            else
            {
                resolvedParameters.add(get(parameterType, stack));
            }
        }
        
        return constructor.newInstance(resolvedParameters.toArray());
    }

    /**
     * Returns a created component.
     */
    @Override
    public Object getComponent(String componentId)
    {
        try
        {
            Class<?> klass = getComponentClass(componentId);
            Object component = klass.newInstance();
            injectAnnotatedFields(component);
            return component;
        }
        catch( InstantiationException | IllegalAccessException | ClassCastException e )
        {
            throw new RuntimeException("Can't create component", e);
        }
    }

    /**
     * todo refactor #36
     * @return
     */
    @Override
    public List<Object> getRequestPreprocessors()
    {
        return requestPreprocessors.stream()
                .map(clazz -> (Object)get(clazz))
                .collect(Collectors.toList());
    }

    @Override
    public boolean hasComponent(String componentId)
    {
        return loadedClasses.containsKey(componentId);
    }

    private Class<?> getComponentClass(String componentId)
    {
        if(!hasComponent(componentId))
        {
            throw new RuntimeException("unknownComponent: " + componentId);
        }
        return loadedClasses.get(componentId);
    }

    @Override
    public void injectAnnotatedFields(Object obj)
    {
        Class<?> cls = obj.getClass();
        for (Class<?> c = cls; c != null; c = c.getSuperclass())
        {
            Field[] fields = c.getDeclaredFields();
            for (Field field : fields)
            {
                if(field.getAnnotation(Inject.class) != null || field.getAnnotation(javax.inject.Inject.class) != null)
                {
                    field.setAccessible(true);
                    try
                    {
                        field.set(obj, get(field.getType()));
                    }
                    catch (IllegalAccessException e)
                    {
                        throw new RuntimeException("Error on injectAnnotatedFields", e);
                    }
                }
            }
        }
    }

    @Override
    public Stage getStage()
    {
        return stage;
    }

    private Class<?> loadClass(String path)
    {
        try
        {
            return Class.forName(path);
        }
        catch (ClassNotFoundException e)
        {
            throw new RuntimeException(e);
        }
    }

}
