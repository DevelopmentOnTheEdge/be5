package com.developmentontheedge.be5.server.helpers;

public interface RememberUserHelper
{
    String rememberUser(String username);

    String getRememberedUser(String id);

    void removeRememberedUser(String id);
}
