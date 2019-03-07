package com.developmentontheedge.be5.test.mocks;

import com.developmentontheedge.be5.server.services.rememberme.PersistentTokenRepository;

public class RememberUserHelperMock implements PersistentTokenRepository
{
    @Override
    public String rememberUser(String username)
    {
        return null;
    }

    @Override
    public String getRememberedUser(String id)
    {
        return null;
    }

    @Override
    public void removeRememberedUser(String id)
    {

    }
}
