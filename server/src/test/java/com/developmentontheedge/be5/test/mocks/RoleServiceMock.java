package com.developmentontheedge.be5.test.mocks;

import com.developmentontheedge.be5.server.authentication.RoleService;

import java.util.Collection;
import java.util.List;

import static org.mockito.Mockito.mock;

public class RoleServiceMock implements RoleService
{
    public static RoleService mock = mock(RoleService.class);

    public static void clearMock()
    {
        mock = mock(RoleService.class);
    }

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
