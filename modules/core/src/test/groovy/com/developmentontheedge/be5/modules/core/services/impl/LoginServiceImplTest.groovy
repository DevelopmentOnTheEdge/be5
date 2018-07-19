package com.developmentontheedge.be5.modules.core.services.impl

import com.developmentontheedge.be5.modules.core.CoreBe5ProjectDbMockTest
import com.developmentontheedge.be5.modules.core.services.LoginService
import org.junit.Before
import org.junit.Test

import javax.inject.Inject

import static org.junit.Assert.assertEquals


class LoginServiceImplTest extends CoreBe5ProjectDbMockTest
{
    @Inject
    LoginService loginService

    @Before
    void init()
    {
        initUserWithRoles('1', '2')
    }

    @Test
    void parseRoles()
    {
        assertEquals Arrays.asList('1', '2'), ((LoginServiceImpl) loginService).parseRoles("('1','2')")
        assertEquals Collections.emptyList(), ((LoginServiceImpl) loginService).parseRoles("()")
        assertEquals Collections.emptyList(), ((LoginServiceImpl) loginService).parseRoles(null)
    }

    @Test
    void testSetCurrentRoles()
    {
        assertEquals(['1', '2'], userInfoProvider.get().currentRoles)
        loginService.setCurrentRoles(['1'])

        assertEquals(['1'], userInfoProvider.get().currentRoles)
    }

    @Test
    void testSetCurrentRolesNotAvailable()
    {
        loginService.setCurrentRoles(['3'])

        assertEquals(['3'], userInfoProvider.get().currentRoles)
    }

}
