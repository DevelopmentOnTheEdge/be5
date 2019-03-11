package com.developmentontheedge.be5.server.authentication.rememberme;

import com.developmentontheedge.be5.server.authentication.AuthenticationException;

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
