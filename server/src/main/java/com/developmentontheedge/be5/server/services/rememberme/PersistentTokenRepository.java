package com.developmentontheedge.be5.server.services.rememberme;

public interface PersistentTokenRepository
{
    String rememberUser(String username);

    String getRememberedUser(String id);

    void removeRememberedUser(String id);
}
