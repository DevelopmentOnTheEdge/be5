package com.developmentontheedge.be5.components;

import com.developmentontheedge.be5.api.Component;
import com.developmentontheedge.be5.api.Request;
import com.developmentontheedge.be5.api.Response;
import com.developmentontheedge.be5.env.Injector;
import com.developmentontheedge.be5.api.helpers.UserInfoHolder;
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
            if (currentRoles != null ? !currentRoles.equals(state.currentRoles) : state.currentRoles != null)
                return false;
            return creationTime != null ? creationTime.equals(state.creationTime) : state.creationTime == null;
        }

        @Override
        public int hashCode()
        {
            int result = (loggedIn ? 1 : 0);
            result = 31 * result + (userName != null ? userName.hashCode() : 0);
            result = 31 * result + (availableRoles != null ? availableRoles.hashCode() : 0);
            result = 31 * result + (currentRoles != null ? currentRoles.hashCode() : 0);
            result = 31 * result + (creationTime != null ? creationTime.hashCode() : 0);
            return result;
        }

        @Override
        public String toString()
        {
            return "State{" +
                    "loggedIn=" + loggedIn +
                    ", userName='" + userName + '\'' +
                    ", availableRoles=" + availableRoles +
                    ", currentRoles=" + currentRoles +
                    ", creationTime=" + creationTime +
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
                        UserInfoHolder.getCurrentRoles(),
                        UserInfoHolder.getUserInfo().getCreationTime()
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
