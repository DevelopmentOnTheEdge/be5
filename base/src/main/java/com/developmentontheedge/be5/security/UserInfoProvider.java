package com.developmentontheedge.be5.security;

import com.developmentontheedge.be5.model.UserInfo;
import com.developmentontheedge.be5.metadata.RoleType;

import java.util.List;
import java.util.Locale;

public interface UserInfoProvider
{
    UserInfo getLoggedUser();

    String getLanguage();

    Locale getLocale();

    String getUserName();

    boolean isLoggedIn();

    List<String> getAvailableRoles();

    List<String> getCurrentRoles();

    String getRemoteAddr();

    default boolean isSystemDeveloper()
    {
        return getCurrentRoles().contains(RoleType.ROLE_SYSTEM_DEVELOPER);
    }

    default boolean isAdmin()
    {
        return getLoggedUser().isAdmin();
    }
}
