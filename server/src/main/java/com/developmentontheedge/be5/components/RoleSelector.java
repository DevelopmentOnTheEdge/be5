package com.developmentontheedge.be5.components;

import com.developmentontheedge.be5.api.Component;
import com.developmentontheedge.be5.api.Request;
import com.developmentontheedge.be5.api.Response;
import com.developmentontheedge.be5.env.Injector;
import com.developmentontheedge.be5.api.helpers.UserInfoHolder;
import com.google.common.base.Splitter;

import java.util.List;

public class RoleSelector implements Component {

    public static class RoleSelectorResponse
    {

        final List<String> availableRoles;
        final List<String> selectedRoles;

        public RoleSelectorResponse(List<String> availableRoles, List<String> selectedRoles)
        {
            this.availableRoles = availableRoles;
            this.selectedRoles = selectedRoles;
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

            RoleSelectorResponse that = (RoleSelectorResponse) o;

            if (availableRoles != null ? !availableRoles.equals(that.availableRoles) : that.availableRoles != null)
                return false;
            return selectedRoles != null ? selectedRoles.equals(that.selectedRoles) : that.selectedRoles == null;
        }

        @Override
        public int hashCode()
        {
            int result = availableRoles != null ? availableRoles.hashCode() : 0;
            result = 31 * result + (selectedRoles != null ? selectedRoles.hashCode() : 0);
            return result;
        }

        @Override
        public String toString()
        {
            return "RoleSelectorResponse{" +
                    "availableRoles=" + availableRoles +
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
            sendInitialData(res);
            return;
        case "select":
            selectRolesAndSendNewState(req, res);
            return;
        default:
            res.sendUnknownActionError();
            return;
        }
    }
    
    private void sendInitialData(Response res)
    {
        res.sendAsRawJson(getState());
    }

    private void selectRolesAndSendNewState(Request req, Response res)
    {
        String roles = req.getOrEmpty("roles");
        UserInfoHolder.selectRoles(Splitter.on(',').splitToList(roles));
        
        res.sendAsRawJson(getState());
    }
    
    private RoleSelectorResponse getState()
    {
        return new RoleSelectorResponse(UserInfoHolder.getAvailableRoles(), UserInfoHolder.getCurrentRoles());
    }

}
