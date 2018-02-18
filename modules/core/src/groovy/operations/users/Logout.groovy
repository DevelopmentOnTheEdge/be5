package users

import com.developmentontheedge.be5.api.services.LoginService
import com.developmentontheedge.be5.components.FrontendConstants
import com.developmentontheedge.be5.env.Inject
import com.developmentontheedge.be5.operation.GOperationSupport
import com.developmentontheedge.be5.operation.OperationResult


class Logout extends GOperationSupport
{
    @Inject LoginService loginService

    @Override
    void invoke(Object parameters) throws Exception
    {
        loginService.logout(request)

        setResult(OperationResult.redirect(FrontendConstants.REFRESH_ALL))
    }
}
