package com.developmentontheedge.be5.modules.core.services;

import java.util.Collection;
import java.util.List;

public interface RoleHelper
{
    void updateCurrentRoles(String userName, Collection<String> roles);

    List<String> getCurrentRoles(String userName);

    List<String> getAvailableRoles(String userName);
}
