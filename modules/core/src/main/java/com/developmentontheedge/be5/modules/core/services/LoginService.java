package com.developmentontheedge.be5.modules.core.services;

import com.developmentontheedge.be5.api.Request;

import java.util.List;


public interface LoginService
{
    boolean loginCheck(String username, String password);

    void saveUser(String username, Request req);

    void setCurrentRoles(List<String> roles);
}
