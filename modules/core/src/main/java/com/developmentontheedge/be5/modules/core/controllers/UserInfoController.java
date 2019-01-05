package com.developmentontheedge.be5.modules.core.controllers;

import com.developmentontheedge.be5.base.security.UserInfoProvider;
import com.developmentontheedge.be5.modules.core.services.LoginService;
import com.developmentontheedge.be5.server.services.UserInfoModelService;
import com.developmentontheedge.be5.server.servlet.support.JsonApiController;
import com.developmentontheedge.be5.web.Request;
import com.google.common.base.Splitter;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

@Singleton
public class UserInfoController extends JsonApiController
{
    private final LoginService loginService;
    private final UserInfoModelService userInfoModelService;
    private final UserInfoProvider userInfoProvider;

    @Inject
    public UserInfoController(LoginService loginService, UserInfoModelService userInfoModelService,
                              UserInfoProvider userInfoProvider)
    {
        this.loginService = loginService;
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
        List<String> roles = Splitter.on(',').splitToList(req.getOrEmpty("roles"));

        List<String> availableCurrentRoles = loginService.getAvailableCurrentRoles(roles,
                userInfoProvider.get().getAvailableRoles());

        loginService.setCurrentRoles(availableCurrentRoles);

        return userInfoProvider.get().getCurrentRoles();
    }
}
