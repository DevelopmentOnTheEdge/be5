package com.developmentontheedge.be5.components;

import static com.google.common.base.Strings.isNullOrEmpty;

import com.developmentontheedge.be5.api.Component;
import com.developmentontheedge.be5.api.Request;
import com.developmentontheedge.be5.api.Response;
import com.developmentontheedge.be5.api.ServiceProvider;
import com.developmentontheedge.be5.api.helpers.UserInfoManager;

public class Login implements Component
{
    
    public static class State
    {
        
        public final boolean loggedIn;

        public State(boolean loggedIn)
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
        // deprecated, the 'state' method should be instead instead
        case "test":
            res.sendAsRawJson(UserInfoManager.get(req, serviceProvider).isLoggedIn());
            return;
        case "state":
            res.sendAsJson("loginState", getState(req, serviceProvider));
            return;
        default:
            res.sendUnknownActionError();
            return;
        }
    }
    
    private State getState(Request req, ServiceProvider serviceProvider)
    {
        return new State(UserInfoManager.get(req, serviceProvider).isLoggedIn());
    }
    
    private void login(Request req, Response res, ServiceProvider serviceProvider)
    {
        String username = req.get("username");
        String password = req.get("password");
        
        if (isNullOrEmpty(username) || isNullOrEmpty(password))
        {
            res.sendError("Empty username or password", "loginError");
            return;
        }
        
        if (!UserInfoManager.get(req, serviceProvider).login(username, password))
        {
            res.sendError("Access denied", "loginError");
            return;
        }
        
        res.sendSuccess();
    }

}
