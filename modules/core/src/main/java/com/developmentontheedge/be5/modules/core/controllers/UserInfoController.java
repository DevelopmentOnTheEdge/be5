package com.developmentontheedge.be5.modules.core.controllers;

import com.developmentontheedge.be5.web.Controller;
import com.developmentontheedge.be5.web.Request;
import com.developmentontheedge.be5.web.Response;
import com.developmentontheedge.be5.servlet.UserInfoHolder;
import com.developmentontheedge.be5.api.support.ControllerSupport;
import com.developmentontheedge.be5.modules.core.services.LoginService;
import com.google.common.base.Splitter;

import javax.inject.Inject;


public class UserInfoController extends ControllerSupport implements Controller
{
    private final LoginService loginService;

    @Inject
    public UserInfoController(LoginService loginService)
    {
        this.loginService = loginService;
    }

    @Override
    public void generate(Request req, Response res)
    {
        switch (req.getRequestUri())
        {
            case "":
                res.sendAsRawJson(loginService.getUserInfoModel());
                return;
            case "selectRoles":
                selectRolesAndSendNewState(req, res);
                return;
            default:
                res.sendUnknownActionError();
            }
    }

    private void selectRolesAndSendNewState(Request req, Response res)
    {
        String roles = req.getOrEmpty("roles");

        loginService.setCurrentRoles(Splitter.on(',').splitToList(roles));

        res.sendAsRawJson(UserInfoHolder.getCurrentRoles());
    }
}
