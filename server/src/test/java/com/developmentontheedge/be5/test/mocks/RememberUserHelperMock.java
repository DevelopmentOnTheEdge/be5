package com.developmentontheedge.be5.test.mocks;

import com.developmentontheedge.be5.server.helpers.RememberUserHelper;

public class RememberUserHelperMock implements RememberUserHelper
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
