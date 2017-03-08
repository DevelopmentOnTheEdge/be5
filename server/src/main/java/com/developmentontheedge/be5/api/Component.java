package com.developmentontheedge.be5.api;

/**
 * <p>This is the general interface for components.</p>
 * 
 * <p>Components are registered in <code>plugin.xml</code> of your application or module, e.g.:
 * 
 * <pre>
 * {@code
 * <plugin>
 *   <extension point="com.developmentontheedge.be5.component">
 *     <component class="com.developmentontheedge.be5.auth.components.SocialLoginComponent" id="socialLogin" name="Social Login" />
 *   </extension>
 * </plugin>
 * }
 * </pre>
 * </p>
 * 
 * <p>The identifier of the component is used to route requests to the component, e.g. <code>GET /api/socialLogin/fasebookAppId</code>
 * will use the <code>socialLogin</code> component and result of calling the {@link Request#getRequestUri()} will be <code>facebookAppId</code>.</p>
 * 
 * <p>Each time a request happens a new component instance is created.
 * If the component implements {@link Configurable} then it will be configured before the run. See {@link Configurable} for more information.</p>
 * 
 * @see Request
 * @see Response
 * @see ServiceProvider
 * @see Configurable
 * @author asko
 */
public interface Component {

    /**
     * <p>Generates some content of any content type. Usually we use {@link Response#sendAsJson(String, Object)} to send the typed JSON response.</p>
     * 
     * <p>Use the service provider to delegate the action to the model.
     * The business-logic must be implemented in the model, not in controllers (components, initializers).</p>
     * 
     * @param req a request
     * @param res a response
     * @param serviceProvider a service provider
     * @see Request
     * @see Response
     * @see ServiceProvider
     */
    void generate(Request req, Response res, ServiceProvider serviceProvider);
}
