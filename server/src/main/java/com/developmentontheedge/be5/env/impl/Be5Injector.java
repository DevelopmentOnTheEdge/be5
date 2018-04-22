package com.developmentontheedge.be5.env.impl;

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
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.developmentontheedge.be5.api.Component;
import com.developmentontheedge.be5.api.RequestPreprocessor;
import com.developmentontheedge.be5.api.exceptions.Be5Exception;
import com.developmentontheedge.be5.env.Binder;
import com.developmentontheedge.be5.api.Configurable;
import com.developmentontheedge.be5.env.Inject;
import com.developmentontheedge.be5.env.Injector;
import com.developmentontheedge.be5.env.Stage;
import com.developmentontheedge.be5.metadata.util.JULLogger;
import com.google.gson.Gson;

import javax.inject.Provider;


public class Be5Injector implements Injector
{
    private static final Logger log = Logger.getLogger(Be5Injector.class.getName());

    private Map<String, Class<?>> loadedClasses = new ConcurrentHashMap<>();
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

        log.info(JULLogger.infoBlock(
                "Load classes: " + binder.getClass().getName() +
                    (!binder.getInfo().isEmpty() ? " - " + binder.getInfo() : "") + "\n" +
                 stage + " stage"));

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

    //todo EgissoLogin egissoLogin = injector.get(EgissoLogin.class); - load component by class?
    //need refactoring add Module as in Guice with methods: bind()

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
        configureIfConfigurable(service, configurations);

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
    public Component getComponent(String componentId)
    {
        try
        {
            Class<?> klass = getComponentClass(componentId);
            Component component = (Component) klass.newInstance();
            configureIfConfigurable(component, configurations);
            injectAnnotatedFields(component);
            return component;
        }
        catch( InstantiationException | IllegalAccessException | ClassCastException e )
        {
            throw Be5Exception.internal(e, "Can't create component");
        }
    }

    @Override
    public List<RequestPreprocessor> getRequestPreprocessors()
    {
        return requestPreprocessors.stream()
                .map(clazz -> (RequestPreprocessor)get(clazz))
                .collect(Collectors.toList());
    }

    @Override
    public boolean hasComponent(String componentId)
    {
        return loadedClasses.containsKey(componentId);
    }

    private Class<?> getComponentClass(String componentId)
    {
        if(!hasComponent(componentId)){
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

//            if(config != null)
//            {
            configurable.configure(config);
//            }else{
//                log.warning("Module '" + object.getClass().getName() + "' not configured.");
//            }
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
                        throw Be5Exception.internal(e);
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
