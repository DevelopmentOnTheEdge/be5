package com.developmentontheedge.be5.components;

import com.developmentontheedge.be5.api.Component;
import com.developmentontheedge.be5.api.Request;
import com.developmentontheedge.be5.api.Response;
import com.developmentontheedge.be5.api.ServiceProvider;
import com.developmentontheedge.be5.api.helpers.UserInfoHolder;
import com.developmentontheedge.be5.metadata.RoleType;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;

import java.util.List;

public class RoleSelector implements Component {

    static class RoleSelectorResponse
    {

        public final List<String> availableRoles;
        public final List<String> selectedRoles;

        public RoleSelectorResponse(List<String> availableRoles, List<String> selectedRoles)
        {
            this.availableRoles = availableRoles;
            this.selectedRoles = selectedRoles;
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
    }
    
    public RoleSelector() {
        /* stateless */
    }
    
    @Override
    public void generate(Request req, Response res, ServiceProvider serviceProvider)
    {
        switch (req.getRequestUri())
        {
        case "":
            sendInitialData(req, res, serviceProvider);
            return;
        case "select":
            selectRolesAndSendNewState(req, res, serviceProvider);
            return;
        default:
            res.sendUnknownActionError();
            return;
        }
    }
    
    private void sendInitialData(Request req, Response res, ServiceProvider serviceProvider) {
        res.sendAsRawJson(getState(req, serviceProvider));
    }

    private void selectRolesAndSendNewState(Request req, Response res, ServiceProvider serviceProvider) {
        String roles = req.getNonEmpty("roles");
        UserInfoHolder.selectRoles(Splitter.on(',').splitToList(roles));
        
        res.sendAsRawJson(getState(req, serviceProvider));
    }
    
    private RoleSelectorResponse getState(Request req, ServiceProvider serviceProvider)
    {
        return new RoleSelectorResponse(UserInfoHolder.getAvailableRoles(), UserInfoHolder.getCurrentRoles());
    }

}
