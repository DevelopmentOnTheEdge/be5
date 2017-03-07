package com.developmentontheedge.be5.api;


/**
 * <p>A component representing the websocket service endpoint.</p>
 * 
 * <p>WebSocket components are registered in the <code>plugin.xml</code>, e.g.:
 * 
 * <pre>
 * {@code
 * <plugin>
 *   <extension point="com.beanexplorer.be5.websocketcomponent">
 *     <component class="ru.biosoft.biostore.docker.VNCProxy" id="vncproxy" name="VNC Proxy"/>
 *   </extension>
 * </plugin>
 * }
 * </pre>
 * </p>
 * 
 * <p>The identifier of the component is used to route requests to the component, e.g. <code>WS /ws/vncproxy</code>
 * will use the <code>vncproxy</code> component.</p>
 * 
 * @see WebSocketContext
 * @see ServiceProvider
 * @author lan
 */
public interface WebSocketComponent
{
    /**
     * Called when the web socket component has been created and the service has been opened.
     */
    public void onOpen(WebSocketContext parameters, ServiceProvider serviceProvider);
    
    /**
     * Called on each message.
     */
    public void onMessage(WebSocketContext parameters, ServiceProvider serviceProvider, byte[] message);
    
    /**
     * Called when the remote socket is closed.
     */
    public void onClose(WebSocketContext parameters, ServiceProvider serviceProvider);
}
