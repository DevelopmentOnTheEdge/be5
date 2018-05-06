package com.developmentontheedge.be5.api;

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
 * @see Request
 * @see Response
 */
public interface Component
{
    /**
     * <p>Generates some content of any content type.</p>
     * 
     * <p>Use the service provider to delegate the action to the model.
     * The business-logic must be implemented in the model, not in controllers (components, initializers).</p>
     * 
     * @param req a request
     * @param res a response
     * @see Request
     * @see Response
     */
    void generate(Request req, Response res);
}
