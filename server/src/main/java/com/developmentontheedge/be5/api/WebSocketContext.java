package com.developmentontheedge.be5.api;

import javax.websocket.Session;

/**
 * Everything that is available in WebSocket components.
 * 
 * @see SessionAccess
 * @see ParametersAccess
 */
public interface WebSocketContext extends SessionAccess, ParametersAccess
{
    /**
     * Returns the current user name.
     * @return current user name
     */
    String getUserName();

    /**
     * Returns the web socket session object.
     * @return web socket session object
     */
    Session getWebSocketSession();
}
