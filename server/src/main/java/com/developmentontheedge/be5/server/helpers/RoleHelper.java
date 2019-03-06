package com.developmentontheedge.be5.server.helpers;

import java.util.Collection;
import java.util.List;

public interface RoleHelper
{
    void updateCurrentRoles(String userName, Collection<String> roles);

    List<String> getCurrentRoles(String userName);

    List<String> getAvailableRoles(String userName);
}
