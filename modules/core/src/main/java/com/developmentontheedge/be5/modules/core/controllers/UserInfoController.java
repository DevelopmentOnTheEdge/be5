package com.developmentontheedge.be5.modules.core.controllers;

import com.developmentontheedge.be5.base.services.UserInfoProvider;
import com.developmentontheedge.be5.modules.core.services.LoginService;
import com.developmentontheedge.be5.server.servlet.support.ApiControllerSupport;
import com.developmentontheedge.be5.web.Controller;
import com.developmentontheedge.be5.web.Request;
import com.developmentontheedge.be5.web.Response;
import com.google.common.base.Splitter;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;


public class UserInfoController extends ApiControllerSupport implements Controller
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
    public void generate(Request req, Response res, String requestSubUrl)
    {
        switch (requestSubUrl)
        {
            case "":
                res.sendAsJson(loginService.getUserInfoModel());
                return;
            case "selectRoles":
                selectRolesAndSendNewState(req, res);
                return;
            default:
                res.sendErrorAsJson("Unknown action", HttpServletResponse.SC_NOT_FOUND);
            }
    }

    private void selectRolesAndSendNewState(Request req, Response res)
    {
        String roles = req.getOrEmpty("roles");

        loginService.setCurrentRoles(Splitter.on(',').splitToList(roles));

        res.sendAsJson(userInfoProvider.get().getCurrentRoles());
    }
}
