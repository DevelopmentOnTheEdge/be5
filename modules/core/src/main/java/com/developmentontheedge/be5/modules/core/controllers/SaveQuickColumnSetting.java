package com.developmentontheedge.be5.modules.core.controllers;

import com.developmentontheedge.be5.base.model.UserInfo;
import com.developmentontheedge.be5.base.services.CoreUtils;
import com.developmentontheedge.be5.base.security.UserInfoProvider;
import com.developmentontheedge.be5.server.servlet.support.JsonApiController;
import com.developmentontheedge.be5.web.Request;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collections;

@Singleton
public class SaveQuickColumnSetting extends JsonApiController
{
    private final CoreUtils coreUtils;
    private final UserInfoProvider userInfoProvider;

    @Inject
    public SaveQuickColumnSetting(CoreUtils coreUtils, UserInfoProvider userInfoProvider)
    {
        this.coreUtils = coreUtils;
        this.userInfoProvider = userInfoProvider;
    }

    @Override
    protected Object generate(Request req, String action)
    {
        UserInfo userInfo = userInfoProvider.getLoggedUser();
        String table_name = req.get("table_name");
        String query_name = req.get("query_name");
        String column_name = req.get("column_name");
        String quick = req.get("quick");

        coreUtils.setColumnSettingForUser(table_name, query_name, column_name, userInfo.getUserName(),
                Collections.singletonMap("quick", quick));

        return "ok";
    }
}
