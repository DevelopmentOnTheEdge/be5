package com.developmentontheedge.be5.server.services.impl;

import com.developmentontheedge.be5.base.UserInfoProvider;
import com.developmentontheedge.be5.base.model.UserInfo;
import com.developmentontheedge.be5.web.SessionConstants;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.servlet.http.HttpSession;


public class UserInfoProviderImpl implements UserInfoProvider, Provider<UserInfo>
{
    private final Provider<HttpSession> session;

    @Inject
    public UserInfoProviderImpl(Provider<HttpSession> session)
    {
        this.session = session;
    }

    @Override
    public UserInfo get()
    {
        return (UserInfo)session.get().getAttribute(SessionConstants.USER_INFO);
    }
}