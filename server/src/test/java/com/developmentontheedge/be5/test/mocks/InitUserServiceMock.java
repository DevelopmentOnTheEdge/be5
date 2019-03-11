package com.developmentontheedge.be5.test.mocks;

import com.developmentontheedge.be5.server.authentication.InitUserService;
import org.mockito.Mockito;

public class InitUserServiceMock implements InitUserService
{
    public static InitUserService mock = Mockito.mock(InitUserService.class);

    @Override
    public void initUser(String username)
    {
        mock.initUser(username);
    }
}
