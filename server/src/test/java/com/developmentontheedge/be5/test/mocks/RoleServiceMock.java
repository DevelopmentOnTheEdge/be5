package com.developmentontheedge.be5.test.mocks;

import com.developmentontheedge.be5.server.services.users.RoleService;
import org.mockito.Mockito;

import java.util.Collection;
import java.util.List;

public class RoleServiceMock implements RoleService
{
    public static RoleService mock = Mockito.mock(RoleService.class);

    @Override
    public void updateCurrentRoles(String userName, Collection<String> roles)
    {
        mock.updateCurrentRoles(userName, roles);
    }

    @Override
    public List<String> getCurrentRoles(String userName)
    {
        return mock.getCurrentRoles(userName);
    }

    @Override
    public List<String> getAvailableRoles(String userName)
    {
        return mock.getAvailableRoles(userName);
    }
}
