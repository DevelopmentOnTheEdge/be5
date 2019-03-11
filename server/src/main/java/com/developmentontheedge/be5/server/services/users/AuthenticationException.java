package com.developmentontheedge.be5.server.services.users;

public abstract class AuthenticationException extends RuntimeException
{
    public AuthenticationException(String msg, Throwable t)
    {
        super(msg, t);
    }

    public AuthenticationException(String msg)
    {
        super(msg);
    }
}
