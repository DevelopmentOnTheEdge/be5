package com.developmentontheedge.be5.modules.core.operations.users;

import com.developmentontheedge.be5.server.FrontendActions;
import com.developmentontheedge.be5.server.authentication.UserInfoModelService;
import com.developmentontheedge.be5.server.authentication.UserService;
import com.developmentontheedge.be5.server.operations.support.GOperationSupport;
import com.developmentontheedge.be5.web.Response;

import javax.inject.Inject;


public class Logout extends GOperationSupport
{
    @Inject
    private UserService userService;
    @Inject
    private UserInfoModelService userInfoModelService;
    @Inject
    private Response response;

    @Override
    public void invoke(Object parameters)
    {
        userService.logout(request, response);
        setResultFinished(FrontendActions.updateUserAndOpenDefaultRoute(userInfoModelService.getUserInfoModel()));
    }
}
