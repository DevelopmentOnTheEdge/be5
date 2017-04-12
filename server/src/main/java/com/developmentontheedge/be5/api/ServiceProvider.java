package com.developmentontheedge.be5.api;

import com.developmentontheedge.be5.api.services.CategoriesService;
import com.developmentontheedge.be5.api.services.DatabaseService;
import com.developmentontheedge.be5.api.services.ExecutorService;
import com.developmentontheedge.be5.api.services.Logger;
import com.developmentontheedge.be5.api.services.LoginService;
import com.developmentontheedge.be5.api.services.Meta;
import com.developmentontheedge.be5.api.services.ProjectProvider;
import com.developmentontheedge.be5.api.services.SqlService;
import com.developmentontheedge.be5.metadata.model.Project;

import java.util.function.Consumer;

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
public interface ServiceProvider
{
    
    /**
     * Returns a service that provides SQL queries execution.
     */
    default SqlService getSqlService()
    {
        return get(SqlService.class);
    }
    
    /**
     * Returns a service that provides database connection.
     */
    default DatabaseService getDatabaseService()
    {
        return get(DatabaseService.class);
    }
    
    /**
     * <summary>Returns a connection from its database service.</summary>
     * 
     * <p>
     * This method should not be used in usual cases, as a database connector
     * can be changed. Usually a service have to keep a database service and get
     * a connector from it right before execution of a query.
     * </p>
     */
    
    /**
     * Returns a service that classifies categories.
     */
    default CategoriesService getCategoriesService()
    {
        return get(CategoriesService.class);
    }
    
    /**
     * Returns a project provider.
     */
    default ProjectProvider getProjectProvider()
    {
        return get(ProjectProvider.class);
    }
    
    /**
     * Returns a project.
     */
    default Project getProject()
    {
        return getProjectProvider().getProject();
    }
    
    /**
     * Returns a service that simplifies work with the project.
     */
    default Meta getMeta()
    {
        return get(Meta.class);
    }

    default ExecutorService getExecutorService()
    {
        return get(ExecutorService.class);
    }
    
    /**
     * Returns a logger service.
     */
    default Logger getLogger()
    {
        return get(Logger.class);
    }

    default LoginService getLoginService()
    {
        return get(LoginService.class);
    }
    
    /**
     * <p>Bind an interface to an implementation class.</p>
     * 
     * <p>This method is not a part of the API as it can be called only before the {@link ServiceProvider#freeze()} is called,
     * that is called before creation of any initializer or component. Services don't have access to the service provider.</p>
     */
    <T, TT extends T> void bind(Class<T> serviceClass, Class<TT> implementationClass, Consumer<TT> initializer);
    
    /**
     * <p>Stops registration.</p>
     * 
     * <p>This method is not a part of the API as it can be called only once. It is called before creation of any initializer of component.
     * Services don't have access to the service provider.</p>
     */
    void freeze();
    
    /**
     * Resolves a service by its class.
     * @see ServiceProvider ServiceProvider for more information
     */
    <T> T get(Class<T> serviceClass);
    
}
