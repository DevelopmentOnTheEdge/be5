package com.developmentontheedge.be5.modules.core.services;

public interface LoginService
{
    boolean loginCheck(String username, char[] password);

    String finalPassword(char[] password);

    /**
     * The {@code users.user_name} a login accepted by {@link #loginCheck} stands for. Override when
     * {@code loginCheck} also accepts another identifier (e.g. an e-mail address): roles are stored by user name.
     */
    default String getUserName(String login)
    {
        return login;
    }
}
