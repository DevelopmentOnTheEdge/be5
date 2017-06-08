package com.developmentontheedge.be5.api;

import com.developmentontheedge.be5.env.Configurable;
import com.developmentontheedge.be5.env.Injector;

/**
 * <p>This is the general interface for components.</p>
 * 
 * <p>Components are registered in <code>context.yaml</code> of your application or module, e.g.:
 * 
 * <pre>
 * {@code
 * context:
 *  components:
 *  - appInfo:  com.developmentontheedge.be5.components.ApplicationInfoComponent
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
 * @see Injector
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
     * @param injector a service provider
     * @see Request
     * @see Response
     * @see Injector
     */
    void generate(Request req, Response res, Injector injector);
}
