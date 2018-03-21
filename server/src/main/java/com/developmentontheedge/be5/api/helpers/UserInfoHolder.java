package com.developmentontheedge.be5.api.helpers;

import com.developmentontheedge.be5.api.Request;
import com.developmentontheedge.be5.api.Session;
import com.developmentontheedge.be5.metadata.RoleType;
import com.developmentontheedge.be5.model.UserInfo;

import java.util.List;
import java.util.Locale;


public class UserInfoHolder
{
    private static final ThreadLocal<UserInfo> threadLocalScope = new  ThreadLocal<>();
    private static final ThreadLocal<Request> requestThreadLocalScope = new  ThreadLocal<>();

    public static void setUserInfo(UserInfo user)
    {
        threadLocalScope.set(user);
    }

    public static UserInfo getUserInfo()
    {
        return threadLocalScope.get();
    }

    public static void setRequest(Request request)
    {
        requestThreadLocalScope.set(request);
    }

    public static Request getRequest()
    {
        return requestThreadLocalScope.get();
    }

    public static Session getSession()
    {
        return getUserInfo().getSession();
    }

    public static String getLanguage()
    {
        return getLocale().getLanguage().toLowerCase();
    }

    public static Locale getLocale()
    {
        return getUserInfo().getLocale();
    }

    public static void changeLanguage(String language)
    {
        getUserInfo().setLocale(new Locale(language));
    }

    public static void selectRoles(List<String> roles)
    {
        getUserInfo().selectRoles(roles);
    }

    public static String getUserName()
    {
        return getUserInfo().getUserName();
    }

    public static boolean isLoggedIn()
    {
        return !RoleType.ROLE_GUEST.equals(getUserInfo().getUserName());
    }

    public static List<String> getAvailableRoles()
    {
        return getUserInfo().getAvailableRoles();
    }

    public static List<String> getCurrentRoles()
    {
        return getUserInfo().getCurrentRoles();
    }

    public static String getRemoteAddr()
    {
        return getUserInfo().getRemoteAddr();
    }

    public static boolean isSystemDeveloper()
    {
        return getCurrentRoles().contains(RoleType.ROLE_SYSTEM_DEVELOPER);
    }

}
