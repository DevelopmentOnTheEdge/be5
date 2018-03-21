package com.developmentontheedge.be5.components;

import com.developmentontheedge.be5.api.Component;
import com.developmentontheedge.be5.api.Request;
import com.developmentontheedge.be5.api.Response;
import com.developmentontheedge.be5.env.Injector;
import com.developmentontheedge.be5.api.helpers.UserInfoHolder;
import com.google.common.base.Splitter;

import java.util.List;


public class UserInfoComponent implements Component
{
    public static class State
    {
        private final boolean loggedIn;
        private final String userName;
        private final List<String> availableRoles;
        private final List<String> selectedRoles;

        public State(boolean loggedIn, String userName, List<String> availableRoles, List<String> selectedRoles)
        {
            this.loggedIn = loggedIn;
            this.userName = userName;
            this.availableRoles = availableRoles;
            this.selectedRoles = selectedRoles;
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

        public List<String> getSelectedRoles()
        {
            return selectedRoles;
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            State state = (State) o;

            if (loggedIn != state.loggedIn) return false;
            if (userName != null ? !userName.equals(state.userName) : state.userName != null) return false;
            if (availableRoles != null ? !availableRoles.equals(state.availableRoles) : state.availableRoles != null)
                return false;
            return selectedRoles != null ? selectedRoles.equals(state.selectedRoles) : state.selectedRoles == null;
        }

        @Override
        public String toString()
        {
            return "State{" +
                    "loggedIn=" + loggedIn +
                    ", userName='" + userName + '\'' +
                    ", availableRoles=" + availableRoles +
                    ", selectedRoles=" + selectedRoles +
                    '}';
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
                        UserInfoHolder.getCurrentRoles()
                ));
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
        UserInfoHolder.selectRoles(Splitter.on(',').splitToList(roles));

        res.sendAsRawJson(UserInfoHolder.getCurrentRoles());
    }
}
