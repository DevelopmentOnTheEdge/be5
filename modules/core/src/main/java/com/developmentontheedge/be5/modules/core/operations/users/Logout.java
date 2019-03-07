package com.developmentontheedge.be5.modules.core.operations.users;

import com.developmentontheedge.be5.server.FrontendActions;
import com.developmentontheedge.be5.server.services.users.UserHelper;
import com.developmentontheedge.be5.server.operations.support.GOperationSupport;
import com.developmentontheedge.be5.server.services.UserInfoModelService;

import javax.inject.Inject;


public class Logout extends GOperationSupport
{
    @Inject
    private UserHelper userHelper;
    @Inject
    private UserInfoModelService userInfoModelService;

    @Override
    public void invoke(Object parameters) throws Exception
    {
        userHelper.logout();
        setResultFinished(FrontendActions.updateUserAndOpenDefaultRoute(userInfoModelService.getUserInfoModel()));
    }
}
