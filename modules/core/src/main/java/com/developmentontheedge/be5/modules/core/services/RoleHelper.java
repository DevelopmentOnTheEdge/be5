package com.developmentontheedge.be5.modules.core.services;

import java.util.List;

public interface RoleHelper
{
    void updateCurrentRoles(String userName, List<String> roles);

    List<String> getCurrentRoles(String userName);

    List<String> getAvailableRoles(String userName);
}
