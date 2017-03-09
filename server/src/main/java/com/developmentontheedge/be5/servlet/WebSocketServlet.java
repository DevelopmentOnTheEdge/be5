package com.developmentontheedge.be5.servlet;

import java.io.IOException;

import javax.websocket.CloseReason;
import javax.websocket.CloseReason.CloseCodes;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import com.developmentontheedge.be5.servlet.Reflection.DynamicObject;

@ServerEndpoint ( value = "/ws/{component}", configurator = WebSocketHttpSessionConfigurator.class, subprotocols = {"binary"} )
public class WebSocketServlet
{

    private static DynamicObject mainServlet;

    public static void setMain(Object mainServletImpl)
    {
        mainServlet = Reflection.on( mainServletImpl );
    }

    @OnOpen
    public void onOpen(Session session)
    {
        if( mainServlet == null )
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
            mainServlet.call( "onWsOpen", session );
        }
        catch( Exception e )
        {
            throw new RuntimeException( e );
        }
    }

    @OnMessage
    public void onMessage(byte[] message, Session session)
    {
        if( mainServlet == null )
            return;
        try
        {
            mainServlet.call( "onWsMessage", message, session );
        }
        catch( Exception e )
        {
            throw new RuntimeException( e );
        }
    }
    
    @OnClose
    public void onClose(Session session)
    {
        if( mainServlet == null )
            return;
        try
        {
            mainServlet.call( "onWsClose", session );
        }
        catch( Exception e )
        {
            throw new RuntimeException( e );
        }
    }
}
