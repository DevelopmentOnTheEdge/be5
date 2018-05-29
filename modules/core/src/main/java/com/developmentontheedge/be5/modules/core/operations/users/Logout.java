package com.developmentontheedge.be5.modules.core.operations.users;

import com.developmentontheedge.be5.modules.core.api.CoreFrontendActions;
import com.developmentontheedge.be5.modules.core.services.LoginService;
import com.developmentontheedge.be5.server.operations.support.GOperationSupport;
import com.developmentontheedge.be5.operation.model.OperationResult;
import com.developmentontheedge.be5.api.helpers.UserHelper;

import javax.inject.Inject;


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
