package com.developmentontheedge.be5.api.services;



public interface LoginService
{
    int login(String user, String password, String passwordKey, String sessionId);
}
