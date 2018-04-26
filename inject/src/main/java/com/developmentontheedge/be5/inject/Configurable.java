package com.developmentontheedge.be5.inject;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * <p>Implement it if you want make your component, initializer or service configurable.</p>
 * 
 * <p>Configuration is placed in the <code>config.yaml</code> in the <code>project</code>.</p>
 * 
 * <p><code>config.yaml</code> content example:
 *  
 * <pre>
 *   config:
 *     components:
 *       registration:
 *         host: smtp.gmail.com
 *         port: 587
 *         sender: 'some email'
 *         password: 'some password'
 *     initializers:
 *       appRepository:
 *         path: apps
 *     services:
 *       docker:
 *         url: 'http://cn01:4243/'
 *         vncHost: 'cn01'
 * </pre>
 * </p>
 * 
 * <p>There are three types of configurations:
 * <ul>
 * <li>component configuration</li>
 * <li>initializer configuration</li>
 * <li>service configuration.</li>
 * </ul>
 * </p>
 * 
 * <p>Any way your class implements <code>{@code Configurable<MyConfiguration>}</code>,
 * and the <code>MyConfiguration</code> will be created and filled automatically, then the <code>void configure(MyConfiguration config)</code> will be called.
 * It is called after newInstance and injectAnnotatedFields
 * or right after creation of the service. The concrete configuration will be parsed with Gson, so it can contain strings, numbers, lists, maps, your POJO classes, etc.
 * </p>
 *
 * @see Initializer
 * @see Configurable#getConfigurationClass()
 * @see Configurable#configure(Object)
 * @author asko
 */
public interface Configurable<T> {
    
    /**
     * <p>Gets a concrete class of configuration that will be created and filled with parameters.
     * Usually this returns the class that is pointed out as the generic parameter.
     * An other class can be returned if the configurable component has a non-trivial inheritance hierarchy.</p>
     * 
     * <p>You must implement it yourself if your class implements the Configurable indirectly.</p>
     * 
     * @see Configurable#configure(Object)
     * @see Configurable
     */
    @SuppressWarnings("unchecked")
    default Class<T> getConfigurationClass()
    {
        for (Type type : getClass().getGenericInterfaces())
        {
            if (type instanceof ParameterizedType)
            {
                ParameterizedType parametrized = (ParameterizedType) type;
                
                /*
                 * If we found configurable, then we can be sure that this is correct class
                 * because it is impossible to implement Configurable twice.
                 */
                if (parametrized.getRawType() == Configurable.class)
                {
                    /*
                     * It is guaranteed that the generic type is only one and it is T.
                     */
                    return (Class<T>) parametrized.getActualTypeArguments()[0];
                }
            }
        }
        
        throw new AssertionError();
    }
    
    /**
     * <p>This will be called right after newInstance and injectAnnotatedFields,
     * {@link Initializer#initialize(InitializerContext, Injector)} or after creation of a service.
     * A part of a configuration file will be parsed to the passed object. This object is created in any case,
     * even if there's no section in the configuration file for this component, therefore it can't be null.</p>
     * 
     * @param config automatically created and filled instance of your configuration class
     * @see Configurable#getConfigurationClass()
     * @see Configurable
     */
    void configure(T config);
    
}