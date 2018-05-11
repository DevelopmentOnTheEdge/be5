package com.developmentontheedge.be5.modules.core.api;

import com.developmentontheedge.be5.model.FrontendAction;
import com.developmentontheedge.be5.modules.core.model.UserInfoModel;


public interface CoreFrontendConstants
{
    String UPDATE_USER_INFO = "UPDATE_USER_INFO";
    String OPEN_DEFAULT_ROUTE = "OPEN_DEFAULT_ROUTE";

    static FrontendAction[] updateUserAndOpenRoute(UserInfoModel userInfoModel)
    {
        return new FrontendAction[]{
                new FrontendAction(UPDATE_USER_INFO, userInfoModel),
                new FrontendAction(OPEN_DEFAULT_ROUTE, null)
        };
    }
}
