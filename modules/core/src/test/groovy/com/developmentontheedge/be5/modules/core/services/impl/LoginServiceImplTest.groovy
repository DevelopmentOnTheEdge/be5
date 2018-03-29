package com.developmentontheedge.be5.modules.core.services.impl

import com.developmentontheedge.be5.api.helpers.UserInfoHolder
import com.developmentontheedge.be5.env.Inject
import com.developmentontheedge.be5.modules.core.services.LoginService
import com.developmentontheedge.be5.test.Be5ProjectTest
import org.junit.Before
import org.junit.Test

import static org.junit.Assert.*


class LoginServiceImplTest extends Be5ProjectTest
{
    @Inject LoginService loginService

    @Before
    void init()
    {
        initUserWithRoles('1', '2')
    }

    @Test
    void parseRoles()
    {
        assertEquals Arrays.asList('1', '2'), ((LoginServiceImpl)loginService).parseRoles("('1','2')")
        assertEquals Collections.emptyList(), ((LoginServiceImpl)loginService).parseRoles("()")
        assertEquals Collections.emptyList(), ((LoginServiceImpl)loginService).parseRoles(null)
    }

    @Test
    void testSetCurrentRoles()
    {
        assertEquals (['1', '2'], UserInfoHolder.currentRoles)
        loginService.setCurrentRoles(['1'])

        assertEquals (['1'], UserInfoHolder.currentRoles)
    }

    @Test
    void testSetCurrentRolesNotAvailable()
    {
        loginService.setCurrentRoles(['3'])

        assertEquals ([], UserInfoHolder.currentRoles)
    }

}