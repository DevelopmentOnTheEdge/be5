package com.developmentontheedge.be5.server.services.rememberme;

import com.developmentontheedge.be5.server.services.users.AuthenticationException;

public class RememberMeAuthenticationException extends AuthenticationException
{
    public RememberMeAuthenticationException(String msg, Throwable t)
    {
        super(msg, t);
    }

    public RememberMeAuthenticationException(String msg)
    {
        super(msg);
    }
}
