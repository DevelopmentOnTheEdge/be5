package com.developmentontheedge.be5.api.services.impl;

import com.developmentontheedge.be5.api.SessionConstants;
import com.developmentontheedge.be5.base.UserInfoProvider;
import com.developmentontheedge.be5.base.model.UserInfo;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.servlet.http.HttpSession;


public class UserInfoProviderImpl implements UserInfoProvider
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
