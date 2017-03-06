package com.developmentontheedge.be5.api.impl;

import java.util.Collections;
import java.util.Map;

import javax.servlet.http.HttpSession;
import javax.websocket.Session;

import com.developmentontheedge.be5.SessionConstants;
import com.developmentontheedge.be5.UserInfo;
import com.developmentontheedge.be5.api.WebSocketContext;
import com.developmentontheedge.be5.util.Delegator;

import one.util.streamex.EntryStream;

public class WebSocketContextImpl implements WebSocketContext
{
    private final Session session;
    private final HttpSession httpSession;
    private final Map<String, String> map;

    public WebSocketContextImpl(Session session)
    {
        this.session = session;
        this.httpSession = Delegator.on( session.getUserProperties().get( HttpSession.class.getName() ), HttpSession.class );
        this.map = Collections.unmodifiableMap( EntryStream.of( session.getRequestParameterMap() )
                .mapValues( list -> list.isEmpty() ? "" : list.get( 0 ) ).toMap() );
    }

    @Override
    public Session getWebSocketSession()
    {
        return session;
    }

    @Override
    public String getSessionId()
    {
        return httpSession == null ? null : httpSession.getId();
    }

    @Override
    public Object getAttribute(String name)
    {
        return httpSession == null ? null : httpSession.getAttribute(name);
    }

    @Override
    public void setAttribute(String name, Object value)
    {
        if(httpSession != null)
            httpSession.setAttribute( name, value );
    }

    @Override
    public Map<String, String> getParameters()
    {
        return map;
    }

    @Override
    public String getUserName()
    {
        if(httpSession == null)
            return null;
        UserInfo ui = ( UserInfo )httpSession.getAttribute( SessionConstants.USER_INFO );
        return ui == null ? null : ui.getUserName();
    }
}
