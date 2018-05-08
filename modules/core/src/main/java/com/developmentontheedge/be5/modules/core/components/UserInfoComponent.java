package com.developmentontheedge.be5.modules.core.components;

import com.developmentontheedge.be5.api.Controller;
import com.developmentontheedge.be5.api.Request;
import com.developmentontheedge.be5.api.Response;
import com.developmentontheedge.be5.api.helpers.UserInfoHolder;
import com.developmentontheedge.be5.api.impl.ControllerSupport;
import com.developmentontheedge.be5.modules.core.services.LoginService;
import com.google.common.base.Splitter;

import com.google.inject.Inject;


public class UserInfoComponent extends ControllerSupport implements Controller
{
    private final LoginService loginService;

    @Inject
    public UserInfoComponent(LoginService loginService)
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
