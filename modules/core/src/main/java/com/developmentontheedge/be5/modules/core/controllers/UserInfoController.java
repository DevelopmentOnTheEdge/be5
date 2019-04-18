package com.developmentontheedge.be5.modules.core.controllers;

import com.developmentontheedge.be5.server.authentication.UserInfoModelService;
import com.developmentontheedge.be5.server.authentication.UserService;
import com.developmentontheedge.be5.server.servlet.support.JsonApiController;
import com.developmentontheedge.be5.web.Request;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Arrays;
import java.util.List;

@Singleton
public class UserInfoController extends JsonApiController
{
    private final UserService userHelper;
    private final UserInfoModelService userInfoModelService;

    @Inject
    public UserInfoController(UserService userHelper, UserInfoModelService userInfoModelService)
    {
        this.userHelper = userHelper;
        this.userInfoModelService = userInfoModelService;
    }

    @Override
    protected Object generate(Request req, String action)
    {
        switch (action)
        {
            case "":
                return userInfoModelService.getUserInfoModel();
            case "selectRoles":
                return selectRolesAndSendNewState(req);
            default:
                return null;
        }
    }

    private Object selectRolesAndSendNewState(Request req)
    {
        List<String> roles = Arrays.asList(req.getOrEmpty("roles").split(","));

        userHelper.setCurrentRoles(roles);

        return userInfoModelService.getUserInfoModel();
    }
}
