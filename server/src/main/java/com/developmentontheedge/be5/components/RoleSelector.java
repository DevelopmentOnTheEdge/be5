package com.developmentontheedge.be5.components;

import java.util.List;

import com.developmentontheedge.be5.DatabaseConstants;
import com.developmentontheedge.be5.api.Component;
import com.developmentontheedge.be5.api.Request;
import com.developmentontheedge.be5.api.Response;
import com.developmentontheedge.be5.api.ServiceProvider;
import com.developmentontheedge.be5.api.helpers.UserInfoManager;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;

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
        final String roles = req.get("roles");
        
        if (roles == null)
        {
            res.sendError("Roles are required");
            return;
        }

        try
        {
            UserInfoManager.get(req, serviceProvider).selectRoles(Splitter.on(',').splitToList(roles));
        }
        catch (Exception e)
        {
            // can't change roles, (TODO) should return a error message to show it to the user
        }
        
        res.sendAsRawJson(getState(req, serviceProvider));
    }
    
    private RoleSelectorResponse getState(Request req, ServiceProvider serviceProvider)
    {
        try
        {
            UserInfoManager user = UserInfoManager.get(req, serviceProvider);
            return new RoleSelectorResponse(user.getAvailableRoles(), user.getCurrentRoles());
        }
        catch (Exception e)
        {
            return new RoleSelectorResponse(ImmutableList.of(DatabaseConstants.ROLE_GUEST), ImmutableList.of(DatabaseConstants.ROLE_GUEST));
        }
    }

}
