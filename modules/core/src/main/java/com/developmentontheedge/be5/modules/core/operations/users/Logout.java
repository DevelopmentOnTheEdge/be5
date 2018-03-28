package com.developmentontheedge.be5.modules.core.operations.users;

import com.developmentontheedge.be5.api.services.LoginService;
import com.developmentontheedge.be5.components.FrontendConstants;
import com.developmentontheedge.be5.env.Inject;
import com.developmentontheedge.be5.operation.GOperationSupport;
import com.developmentontheedge.be5.operation.OperationResult;


public class Logout extends GOperationSupport
{
    @Inject protected LoginService loginService;

    @Override
    public void invoke(Object parameters) throws Exception
    {
        loginService.logout(request);

        setResult(OperationResult.finished(FrontendConstants.REFRESH_ALL));
    }
}
