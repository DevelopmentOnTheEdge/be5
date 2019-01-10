package com.developmentontheedge.be5.server.servlet;

import com.developmentontheedge.be5.model.UserInfo;
import com.developmentontheedge.be5.security.UserInfoHolder;
import com.developmentontheedge.be5.server.SessionConstants;
import org.junit.Test;

import javax.servlet.ServletRequest;
import javax.servlet.ServletRequestEvent;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import static com.google.common.collect.ImmutableList.of;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AuthenticationPropagationListenerTest
{
    @Test
    public void requestInitialized()
    {
        UserInfoHolder.setLoggedUser(null);
        UserInfo userInfo = new UserInfo("test", of("test"), of("test"));
        ServletRequestEvent servletRequestEvent = mock(ServletRequestEvent.class);
        ServletRequest servletRequest = mock(HttpServletRequest.class);
        HttpSession httpSession = mock(HttpSession.class);
        when(servletRequestEvent.getServletRequest()).thenReturn(servletRequest);
        when(((HttpServletRequest) servletRequest).getSession(false)).thenReturn(httpSession);
        when(httpSession.getAttribute(SessionConstants.USER_INFO)).thenReturn(userInfo);
        new AuthenticationPropagationListener().requestInitialized(servletRequestEvent);
        assertEquals(userInfo, UserInfoHolder.getLoggedUser());
    }

    @Test
    public void requestDestroyed()
    {
        UserInfoHolder.setLoggedUser(new UserInfo("test", of("test"), of("test")));
        new AuthenticationPropagationListener().requestDestroyed(null);
        assertNull(UserInfoHolder.getLoggedUser());
    }
}
