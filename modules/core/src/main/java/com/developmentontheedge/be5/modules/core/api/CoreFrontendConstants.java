package com.developmentontheedge.be5.modules.core.api;

import com.developmentontheedge.be5.modules.core.model.UserInfoModel;
import com.google.common.collect.ImmutableMap;

import java.util.Map;

public interface CoreFrontendConstants
{
    String UPDATE_USER_INFO = "UPDATE_USER_INFO";
    String OPEN_DEFAULT_ROUTE = "OPEN_DEFAULT_ROUTE";

    static Map<String, Object> updateUserAndOpenRoute(UserInfoModel userInfoModel)
    {
        return ImmutableMap.of(
                UPDATE_USER_INFO, userInfoModel,
                OPEN_DEFAULT_ROUTE, true
        );
    }
}
