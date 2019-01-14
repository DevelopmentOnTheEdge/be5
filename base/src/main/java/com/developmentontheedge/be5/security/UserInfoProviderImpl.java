package com.developmentontheedge.be5.security;

import com.developmentontheedge.be5.metadata.RoleType;

import java.util.List;
import java.util.Locale;


public class UserInfoProviderImpl implements UserInfoProvider
{
    @Override
    public UserInfo getLoggedUser()
    {
        return UserInfoHolder.getLoggedUser();
    }

    @Override
    public String getLanguage()
    {
        return getLocale().getLanguage().toLowerCase();
    }

    @Override
    public Locale getLocale()
    {
        return getLoggedUser().getLocale();
    }

    @Override
    public String getUserName()
    {
        return getLoggedUser().getUserName();
    }

    @Override
    public boolean isLoggedIn()
    {
        return !RoleType.ROLE_GUEST.equals(getLoggedUser().getUserName());
    }

    @Override
    public List<String> getAvailableRoles()
    {
        return getLoggedUser().getAvailableRoles();
    }

    @Override
    public List<String> getCurrentRoles()
    {
        return getLoggedUser().getCurrentRoles();
    }

    @Override
    public String getRemoteAddr()
    {
        return getLoggedUser().getRemoteAddr();
    }
}
