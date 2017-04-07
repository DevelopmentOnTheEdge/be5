package com.developmentontheedge.be5.api.services;


import com.developmentontheedge.be5.api.Request;

public interface LoginService
{

    boolean login(Request req, String user, String password);

    /**
     * Tries to log out.
     */
    void logout(Request req);

}
