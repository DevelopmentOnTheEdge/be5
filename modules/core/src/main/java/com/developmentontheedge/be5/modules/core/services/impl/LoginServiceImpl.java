package com.developmentontheedge.be5.modules.core.services.impl;

import com.developmentontheedge.be5.database.DbService;
import com.developmentontheedge.be5.modules.core.services.LoginService;

import javax.inject.Inject;
import java.util.Objects;
import java.util.logging.Logger;


public class LoginServiceImpl implements LoginService
{
    public static final Logger log = Logger.getLogger(LoginServiceImpl.class.getName());

    protected final DbService db;

    @Inject
    public LoginServiceImpl(DbService db)
    {
        this.db = db;
    }

    @Override
    public boolean loginCheck(String username, char[] rawPassword)
    {
        Objects.requireNonNull(username);
        Objects.requireNonNull(rawPassword);
        return db.countFrom("users WHERE user_name = ? AND user_pass = ?",
                username, new String(rawPassword)) == 1L;
    }

    @Override
    public String finalPassword(char[] password)
    {
        return new String(password);
    }
}
