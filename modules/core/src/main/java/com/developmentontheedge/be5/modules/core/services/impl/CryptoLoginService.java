package com.developmentontheedge.be5.modules.core.services.impl;

import com.developmentontheedge.be5.database.DbService;
import com.developmentontheedge.be5.exceptions.Be5Exception;
import com.developmentontheedge.be5.modules.core.services.LoginService;

import javax.inject.Inject;
import java.util.Objects;

public class CryptoLoginService implements LoginService
{
    private Pbkdf2PasswordEncoder passwordEncoder;

    protected final DbService db;

    @Inject
    public CryptoLoginService(DbService db)
    {
        this.db = db;
        passwordEncoder = new Pbkdf2PasswordEncoder();
    }

    public CryptoLoginService(DbService db, int iterations, int desiredKeyLen )
    {
        this.db = db;
        passwordEncoder = new Pbkdf2PasswordEncoder( iterations, desiredKeyLen );
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
