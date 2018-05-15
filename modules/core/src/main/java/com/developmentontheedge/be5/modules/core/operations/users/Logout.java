package com.developmentontheedge.be5.modules.core.operations.users;

import com.developmentontheedge.be5.modules.core.api.CoreFrontendActions;
import com.developmentontheedge.be5.modules.core.services.LoginService;
import com.developmentontheedge.be5.operation.support.GOperationSupport;
import com.developmentontheedge.be5.operation.OperationResult;
import com.developmentontheedge.be5.api.helpers.UserHelper;

import com.google.inject.Inject;


public class Logout extends GOperationSupport
{
    @Inject protected UserHelper userHelper;
    @Inject protected LoginService loginService;

    @Override
    public void invoke(Object parameters) throws Exception
    {
        userHelper.logout(request);

        setResult(OperationResult.finished(null,
                CoreFrontendActions.updateUserAndOpenDefaultRoute(loginService.getUserInfoModel())));
    }
}
