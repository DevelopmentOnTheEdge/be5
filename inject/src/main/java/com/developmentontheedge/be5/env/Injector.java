package com.developmentontheedge.be5.env;

import java.util.List;

/**
 * <p>The service provider is the general source of the business-logic objects, services.</p>
 * 
 * <p>Components and initializers should not work with model directly, they should delegate all actions to services.</p>
 * 
 * <p>Services are registered in <code>context.yaml</code>, e.g.:
 * 
 * <p>Interface is the public part of the service and should be exported from the module.
 * Implementation is the class that implements the interface and should be hidden from the users of the service.</p>
 * 
 * <p>Implementation of the service can depend on other services,
 * required services are defined as parameters of the constructor of the service implementation, e.g.:
 * 
 * <pre>
 * public class SocialLoginImpl implements SocialLogin
 * {
 *     private final GoogleSignIn googleSignIn;
 *     private final Facebook facebook;
 *     
 *     public SocialLoginImpl(GoogleSignIn googleSignIn, Facebook facebook) // <- these are required services
 *     {
 *         this.googleSignIn = googleSignIn;
 *         this.facebook = facebook;
 *     }
 *     
 *     // interface implementation
 * }
 * </pre>
 * </p>
 * 
 * <p>Services can be configurable, in this case service must implement the {@link Configurable}.
 * See the documentation of the {@link Configurable} for more information.</p>
 * 
 * @see Configurable
 */
public interface Injector
{
    /**
     * Resolves a service by its class.
     * @see Injector ServiceProvider for more information
     */
    <T> T get(Class<T> serviceClass);

    Object getComponent(String componentId);

    List<Object> getRequestPreprocessors();

    boolean hasComponent(String componentId);

    void injectAnnotatedFields(Object obj);

    Stage getStage();
}