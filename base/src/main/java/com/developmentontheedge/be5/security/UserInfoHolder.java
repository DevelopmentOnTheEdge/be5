package com.developmentontheedge.be5.security;

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
