package com.developmentontheedge.be5.server.services.rememberme;

public class InvalidCookieException extends RememberMeAuthenticationException
{
    public InvalidCookieException(String message)
    {
        super(message);
    }
}
