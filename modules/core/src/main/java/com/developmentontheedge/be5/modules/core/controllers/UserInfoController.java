package com.developmentontheedge.be5.modules.core.controllers;

import com.developmentontheedge.be5.base.services.UserInfoProvider;
import com.developmentontheedge.be5.modules.core.services.LoginService;
import com.developmentontheedge.be5.server.servlet.support.JsonApiController;
import com.developmentontheedge.be5.web.Request;
import com.google.common.base.Splitter;

import javax.inject.Inject;


public class UserInfoController extends JsonApiController
{
    private final LoginService loginService;
    private final UserInfoProvider userInfoProvider;

    @Inject
    public UserInfoController(LoginService loginService, UserInfoProvider userInfoProvider)
    {
        this.loginService = loginService;
        this.userInfoProvider = userInfoProvider;
    }

    @Override
    public Object generate(Request req, String requestSubUrl)
    {
        switch (requestSubUrl)
        {
            case "":
                return loginService.getUserInfoModel();
            case "selectRoles":
                return selectRolesAndSendNewState(req);
            default:
                return null;
        }
    }

    private Object selectRolesAndSendNewState(Request req)
    {
        String roles = req.getOrEmpty("roles");

        loginService.setCurrentRoles(Splitter.on(',').splitToList(roles));

        return userInfoProvider.get().getCurrentRoles();
    }
}
