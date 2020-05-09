package com.developmentontheedge.be5.modules.core.services.impl;

import com.developmentontheedge.be5.database.DbService;
import com.developmentontheedge.be5.exceptions.Be5Exception;
import com.developmentontheedge.be5.modules.core.services.LoginService;

import javax.inject.Inject;
import java.util.Objects;

public class CryptoLoginService implements LoginService
{
    private static final Pbkdf2PasswordEncoder passwordEncoder = new Pbkdf2PasswordEncoder();

    protected final DbService db;

    @Inject
    public CryptoLoginService(DbService db)
    {
        this.db = db;
    }

    @Override
    public boolean loginCheck(String username, char[] rawPassword)
    {
        Objects.requireNonNull(username);
        Objects.requireNonNull(rawPassword);
        String storedPassword = db.oneString("SELECT user_pass FROM users WHERE user_name = ?", username);
        if (storedPassword == null) return false;
        try
        {
            return passwordEncoder.check(rawPassword, storedPassword);
        }
        catch (Exception e)
        {
            throw Be5Exception.internal(e);
        }
    }

    @Override
    public String finalPassword(char[] rawPassword)
    {
        Objects.requireNonNull(rawPassword);
        try
        {
            return passwordEncoder.encode(rawPassword);
        }
        catch (Exception e)
        {
            throw Be5Exception.internal(e);
        }
    }
}
