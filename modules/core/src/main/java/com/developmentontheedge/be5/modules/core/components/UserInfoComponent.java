package com.developmentontheedge.be5.modules.core.components;

import com.developmentontheedge.be5.api.Component;
import com.developmentontheedge.be5.api.Request;
import com.developmentontheedge.be5.api.Response;
import com.developmentontheedge.be5.inject.Injector;
import com.developmentontheedge.be5.api.helpers.UserInfoHolder;
import com.developmentontheedge.be5.modules.core.services.LoginService;
import com.google.common.base.Splitter;

import java.util.Date;
import java.util.List;


public class UserInfoComponent implements Component
{
    public static class State
    {
        private final boolean loggedIn;
        private final String userName;
        private final List<String> availableRoles;
        private final List<String> currentRoles;
        private final Date creationTime;

        public State(boolean loggedIn, String userName, List<String> availableRoles, List<String> currentRoles, Date creationTime)
        {
            this.loggedIn = loggedIn;
            this.userName = userName;
            this.availableRoles = availableRoles;
            this.currentRoles = currentRoles;
            this.creationTime = creationTime;
        }

        public boolean isLoggedIn()
        {
            return loggedIn;
        }

        public String getUserName()
        {
            return userName;
        }

        public List<String> getAvailableRoles()
        {
            return availableRoles;
        }

        public List<String> getCurrentRoles()
        {
            return currentRoles;
        }

        public Date getCreationTime()
        {
            return creationTime;
        }
    }

    @Override
    public void generate(Request req, Response res, Injector injector)
    {
        switch (req.getRequestUri())
        {
            case "":
                res.sendAsRawJson(new State(
                        UserInfoHolder.isLoggedIn(),
                        UserInfoHolder.getUserName(),
                        UserInfoHolder.getAvailableRoles(),
                        UserInfoHolder.getCurrentRoles(),
                        UserInfoHolder.getUserInfo().getCreationTime()
                ));
                return;
            case "selectRoles":
                selectRolesAndSendNewState(req, res, injector);
                return;
            default:
                res.sendUnknownActionError();
            }
    }

    private void selectRolesAndSendNewState(Request req, Response res, Injector injector)
    {
        String roles = req.getOrEmpty("roles");

        injector.get(LoginService.class).setCurrentRoles(Splitter.on(',').splitToList(roles));

        res.sendAsRawJson(UserInfoHolder.getCurrentRoles());
    }
}
