package com.developmentontheedge.be5.server.authentication;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public interface RoleService
{
    void updateCurrentRoles(String userName, Collection<String> roles);

    List<String> getCurrentRoles(String userName);

    List<String> getAvailableRoles(String userName);

    default List<String> getAvailableCurrentRoles(List<String> newRoles, List<String> availableRoles)
    {
        if (availableRoles.size() == 0) throw new IllegalArgumentException("User must have at least one role.");
        List<String> finalNewRoles = newRoles.stream()
                .filter(availableRoles::contains)
                .collect(Collectors.toList());
        if (finalNewRoles.size() > 0)
        {
            return finalNewRoles;
        }
        else
        {
            return Collections.singletonList(availableRoles.get(0));
        }
    }
}
