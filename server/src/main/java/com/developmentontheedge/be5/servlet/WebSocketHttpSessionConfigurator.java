package com.developmentontheedge.be5.servlet;

import javax.servlet.http.HttpSession;
import javax.websocket.HandshakeResponse;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpointConfig;

/**
 * http://stackoverflow.com/a/17994303/4856258
 */
public class WebSocketHttpSessionConfigurator extends ServerEndpointConfig.Configurator
{
//    @Override
//    public void modifyHandshake(ServerEndpointConfig config,
//                                HandshakeRequest request,
//                                HandshakeResponse response)
//    {
//        HttpSession httpSession = (HttpSession)request.getHttpSession();
//        if(httpSession != null)
//            config.getUserProperties().put(HttpSession.class.getName(),httpSession);
//    }
}