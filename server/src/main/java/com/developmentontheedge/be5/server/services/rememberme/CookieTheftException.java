package com.developmentontheedge.be5.server.services.rememberme;

public class CookieTheftException extends RememberMeAuthenticationException
{
    public CookieTheftException(String message)
    {
        super(message);
    }
}
