package com.developmentontheedge.be5.api.services;


import com.developmentontheedge.be5.api.Request;
import com.developmentontheedge.be5.api.ServiceProvider;
import com.developmentontheedge.be5.model.UserInfo;

import java.util.List;
import java.util.Locale;

public interface LoginService
{
    boolean login(Request req, String user, String password);

    void logout(Request req);

    void initGuest(Request req);

    void setLanguage(Locale locale);

    UserInfo saveUser(String userName, List<String> availableRoles, Locale locale);
}
