package com.developmentontheedge.be5.test.mocks;

import com.developmentontheedge.be5.server.services.users.RoleHelper;

import java.util.Collection;
import java.util.List;

public class RoleHelperMock implements RoleHelper
{
    @Override
    public void updateCurrentRoles(String userName, Collection<String> roles)
    {

    }

    @Override
    public List<String> getCurrentRoles(String userName)
    {
        return null;
    }

    @Override
    public List<String> getAvailableRoles(String userName)
    {
        return null;
    }
}
