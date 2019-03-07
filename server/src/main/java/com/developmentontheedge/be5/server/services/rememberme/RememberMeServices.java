package com.developmentontheedge.be5.server.services.rememberme;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface RememberMeServices
{
    void onLoginSuccess(HttpServletRequest rawRequest, HttpServletResponse rawResponse, String username);

    String autoLogin(HttpServletRequest request, HttpServletResponse response);

    void logout(HttpServletRequest request, HttpServletResponse response, String username);
}
