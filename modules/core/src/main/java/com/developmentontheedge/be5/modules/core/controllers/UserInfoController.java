package com.developmentontheedge.be5.modules.core.controllers;

import com.developmentontheedge.be5.security.UserInfoProvider;
import com.developmentontheedge.be5.server.services.users.UserService;
import com.developmentontheedge.be5.server.services.UserInfoModelService;
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
    private final UserInfoProvider userInfoProvider;

    @Inject
    public UserInfoController(UserService userHelper, UserInfoModelService userInfoModelService,
                              UserInfoProvider userInfoProvider)
    {
        this.userHelper = userHelper;
        this.userInfoModelService = userInfoModelService;
        this.userInfoProvider = userInfoProvider;
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

        List<String> availableCurrentRoles = userHelper.getAvailableCurrentRoles(roles,
                userInfoProvider.getAvailableRoles());

        userHelper.setCurrentRoles(availableCurrentRoles);

        return userInfoProvider.getCurrentRoles();
    }
}
