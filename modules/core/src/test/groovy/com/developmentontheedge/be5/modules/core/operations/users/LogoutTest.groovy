package com.developmentontheedge.be5.modules.core.operations.users

import com.developmentontheedge.be5.web.Request
import com.developmentontheedge.be5.web.Session
import com.developmentontheedge.be5.metadata.RoleType
import com.developmentontheedge.be5.modules.core.CoreBe5ProjectDbMockTest
import com.developmentontheedge.be5.server.model.FrontendAction
import com.developmentontheedge.be5.modules.core.api.CoreFrontendActions
import com.developmentontheedge.be5.modules.core.model.UserInfoModel
import com.developmentontheedge.be5.operation.model.OperationStatus
import org.junit.Test

import static org.junit.Assert.assertEquals
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.verify
import static org.mockito.Mockito.when


class LogoutTest extends CoreBe5ProjectDbMockTest
{
    @Test
    void logout()
    {
        def request = mock(Request.class)
        //userInfoProvider.get().setRequest(request)

        def session = mock(Session.class)
        when(request.getSession()).thenReturn(session)

        def second = generateOperation(createOperation("users", "All records", "Logout", "")).getSecond()

        //verify(session).invalidate()

        assertEquals(RoleType.ROLE_GUEST, userInfoProvider.get().getUserName())
        assertEquals([RoleType.ROLE_GUEST], userInfoProvider.get().getAvailableRoles())

        assertEquals OperationStatus.FINISHED, second.getStatus()
        assertEquals null, second.getMessage()
        def actions = (FrontendAction[]) second.getDetails()

        assertEquals(CoreFrontendActions.UPDATE_USER_INFO, actions[0].getType())

        def userInfoModel = (UserInfoModel) actions[0].getValue()
        assertEquals RoleType.ROLE_GUEST, userInfoModel.getUserName()
        assertEquals([RoleType.ROLE_GUEST], userInfoModel.getAvailableRoles())

        assertEquals(CoreFrontendActions.OPEN_DEFAULT_ROUTE, actions[1].getType())
    }

}