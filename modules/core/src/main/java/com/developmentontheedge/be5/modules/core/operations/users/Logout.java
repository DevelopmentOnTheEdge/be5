package com.developmentontheedge.be5.modules.core.operations.users;

import com.developmentontheedge.be5.api.FrontendConstants;
import com.developmentontheedge.be5.env.Inject;
import com.developmentontheedge.be5.operation.GOperationSupport;
import com.developmentontheedge.be5.operation.OperationResult;
import com.developmentontheedge.be5.api.helpers.UserHelper;


public class Logout extends GOperationSupport
{
    @Inject protected UserHelper userHelper;

    @Override
    public void invoke(Object parameters) throws Exception
    {
        userHelper.logout(request);

        setResult(OperationResult.finished(null, FrontendConstants.UPDATE_USER_INFO));
    }
}
