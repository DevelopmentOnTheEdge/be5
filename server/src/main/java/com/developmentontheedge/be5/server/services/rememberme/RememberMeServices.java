package com.developmentontheedge.be5.server.services.rememberme;

import com.developmentontheedge.be5.web.Request;
import com.developmentontheedge.be5.web.Response;

public interface RememberMeServices
{
    void onLoginSuccess(Request rawRequest, Response rawResponse, String username);

    String autoLogin(Request request, Response response);

    void logout(Request request, Response response, String username);
}
