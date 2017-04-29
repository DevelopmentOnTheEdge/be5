package com.developmentontheedge.be5.modules.core.components;

import com.developmentontheedge.be5.api.Component;
import com.developmentontheedge.be5.api.Request;
import com.developmentontheedge.be5.api.Response;
import com.developmentontheedge.be5.api.ServiceProvider;
import com.developmentontheedge.be5.api.helpers.UserInfoHolder;
import com.google.common.base.Strings;

import java.util.logging.Logger;

public class Login implements Component
{
    private static final Logger log = Logger.getLogger(Login.class.getName());

    public static class State
    {
        final boolean loggedIn;

        State(boolean loggedIn)
        {
            this.loggedIn = loggedIn;
        }
        
    }
    
    @Override
    public void generate(Request req, Response res, ServiceProvider serviceProvider)
    {
        switch (req.getRequestUri())
        {
        case "":
            login(req, res, serviceProvider);
            return;
        case "state":
            res.sendAsJson("loginState", new State(UserInfoHolder.isLoggedIn()));
            return;
        default:
            res.sendUnknownActionError();
        }
    }

    private void login(Request req, Response res, ServiceProvider serviceProvider)
    {
        String username = req.get("username");
        String password = req.get("password");
        
        if (Strings.isNullOrEmpty(username) || Strings.isNullOrEmpty(password))
        {
            res.sendError("Empty username or password", "loginError");
            return;
        }
        
        if (!serviceProvider.getLoginService().login(req, username, password))
        {
            res.sendError("Access denied", "loginError");
            return;
        }

        res.sendSuccess();
    }

}
