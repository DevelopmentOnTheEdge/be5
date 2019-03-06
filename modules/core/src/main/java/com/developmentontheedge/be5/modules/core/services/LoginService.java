package com.developmentontheedge.be5.modules.core.services;

public interface LoginService
{
    boolean loginCheck(String username, char[] password);

    String finalPassword(char[] password);
}
