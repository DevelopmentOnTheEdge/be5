package com.developmentontheedge.be5.modules.core.components;

import com.developmentontheedge.be5.api.Component;
import com.developmentontheedge.be5.api.Request;
import com.developmentontheedge.be5.api.Response;
import com.developmentontheedge.be5.env.Injector;
import com.developmentontheedge.be5.api.helpers.UserInfoHolder;
import com.developmentontheedge.be5.model.jsonapi.ResourceData;

import java.util.logging.Logger;


@Deprecated
public class Login implements Component
{
    private static final Logger log = Logger.getLogger(Login.class.getName());

    //todo move
    public static class State
    {
        final boolean loggedIn;

        public State(boolean loggedIn)
        {
            this.loggedIn = loggedIn;
        }

        public boolean isLoggedIn()
        {
            return loggedIn;
        }
    }
    
    @Override
    public void generate(Request req, Response res, Injector injector)
    {
        switch (req.getRequestUri())
        {
            case "state":
                res.sendAsJson(new ResourceData("loginState", new State(UserInfoHolder.isLoggedIn()), null), null);
                return;
            default:
                res.sendUnknownActionError();
            }
    }
}
