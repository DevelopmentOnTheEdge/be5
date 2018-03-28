package com.developmentontheedge.be5.modules.core.operations.users

import com.developmentontheedge.be5.api.Request
import com.developmentontheedge.be5.api.Session
import com.developmentontheedge.be5.api.helpers.UserInfoHolder
import com.developmentontheedge.be5.components.FrontendConstants

import com.developmentontheedge.be5.operation.OperationStatus
import com.developmentontheedge.be5.test.SqlMockOperationTest
import org.junit.Test

import static org.junit.Assert.assertEquals
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.verify
import static org.mockito.Mockito.when


class LogoutTest extends SqlMockOperationTest
{
    @Test
    void logout()
    {
        def request = mock(Request.class)
        UserInfoHolder.setRequest(request)

        def session = mock(Session.class)
        when(request.getSession()).thenReturn(session)

        def second = generateOperation(createOperation("users", "", "Logout", "")).getSecond()

        verify(session).invalidate()

        assertEquals(null, UserInfoHolder.getUserInfo())

        assertEquals OperationStatus.FINISHED, second.getStatus()
        assertEquals FrontendConstants.UPDATE_USER_INFO, second.getMessage()
    }

}