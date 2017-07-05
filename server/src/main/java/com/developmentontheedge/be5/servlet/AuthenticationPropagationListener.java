package com.developmentontheedge.be5.servlet;

import com.developmentontheedge.be5.model.UserInfo;
import com.developmentontheedge.be5.api.helpers.UserInfoHolder;
import com.developmentontheedge.be5.metadata.SessionConstants;

import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class AuthenticationPropagationListener implements ServletRequestListener
{

    @Override
    public void requestInitialized(ServletRequestEvent event) {
        HttpServletRequest request = (HttpServletRequest) event.getServletRequest();
        HttpSession session = request.getSession(false);
        if (session == null) {
            return;
        }
        UserInfo user = (UserInfo) session.getAttribute(SessionConstants.USER_INFO);
        UserInfoHolder.setUserInfo(user);
    }

    @Override
    public void requestDestroyed(ServletRequestEvent event) {
        UserInfoHolder.setUserInfo(null);
    }

}