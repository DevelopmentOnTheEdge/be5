package com.developmentontheedge.be5.server.authentication;

import java.util.Collection;
import java.util.List;

public interface RoleService
{
    void updateCurrentRoles(String userName, Collection<String> roles);

    List<String> getCurrentRoles(String userName);

    List<String> getAvailableRoles(String userName);
}
