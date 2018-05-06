package com.developmentontheedge.be5.modules.core.components;

import com.developmentontheedge.be5.api.Component;
import com.developmentontheedge.be5.api.Request;
import com.developmentontheedge.be5.api.Response;
import com.developmentontheedge.be5.api.helpers.UserInfoHolder;
import com.developmentontheedge.be5.modules.core.services.LoginService;
import com.google.common.base.Splitter;


public class UserInfoComponent implements Component
{
    private final LoginService loginService;

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
