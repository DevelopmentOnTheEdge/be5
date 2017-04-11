package com.developmentontheedge.be5.servlet;

import java.io.IOException;

import javax.websocket.CloseReason;
import javax.websocket.CloseReason.CloseCodes;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;


@ServerEndpoint ( value = "/ws/{component}", configurator = WebSocketHttpSessionConfigurator.class, subprotocols = {"binary"} )
public class WebSocketServlet
{

    private static MainServlet mainServletInstance;

    public static void setMain(MainServlet mainServlet)
    {
        mainServletInstance = mainServlet;
    }

    @OnOpen
    public void onOpen(Session session)
    {
        if( mainServletInstance == null )
        {
            try
            {
                session.close( new CloseReason( CloseCodes.getCloseCode( CloseCodes.TRY_AGAIN_LATER.getCode() ),
                        "Server is not initialized yet" ) );
            }
            catch( IOException e )
            {
                throw new RuntimeException( e );
            }
            return;
        }
        try
        {
            mainServletInstance.onWsOpen( session );
        }
        catch( Exception e )
        {
            throw new RuntimeException( e );
        }
    }

    @OnMessage
    public void onMessage(byte[] message, Session session)
    {
        if( mainServletInstance == null )
            return;
        try
        {
            mainServletInstance.onWsMessage( message, session );
        }
        catch( Exception e )
        {
            throw new RuntimeException( e );
        }
    }
    
    @OnClose
    public void onClose(Session session)
    {
        if( mainServletInstance == null )
            return;
        try
        {
            mainServletInstance.onWsClose( session );
        }
        catch( Exception e )
        {
            throw new RuntimeException( e );
        }
    }
}
