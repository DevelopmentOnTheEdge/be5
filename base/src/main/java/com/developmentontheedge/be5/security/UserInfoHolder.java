package com.developmentontheedge.be5.security;

import com.developmentontheedge.be5.model.UserInfo;

public class UserInfoHolder
{
    private static final ThreadLocal<UserInfo> threadLocalScope = new ThreadLocal<>();

    public static UserInfo getLoggedUser()
    {
        return threadLocalScope.get();
    }

    public static void setLoggedUser(UserInfo user)
    {
        threadLocalScope.set(user);
    }
}
