package com.developmentontheedge.be5.modules.core.services.impl;

import com.developmentontheedge.be5.base.exceptions.Be5Exception;
import com.developmentontheedge.be5.base.services.UserInfoProvider;
import com.developmentontheedge.be5.database.DbService;
import com.developmentontheedge.be5.modules.core.services.RoleHelper;
import com.developmentontheedge.be5.server.helpers.MenuHelper;
import com.developmentontheedge.be5.server.helpers.UserHelper;

import javax.inject.Inject;
import java.util.Objects;

public class CryptoLoginService extends LoginServiceImpl
{
    private static final Pbkdf2PasswordEncoder passwordEncoder = new Pbkdf2PasswordEncoder();

    @Inject
    public CryptoLoginService(DbService db, UserHelper userHelper, MenuHelper menuHelper,
                              RoleHelper roleHelper, UserInfoProvider userInfoProvider)
    {
        super(db, userHelper, menuHelper, roleHelper, userInfoProvider);
    }

    @Override
    public boolean loginCheck(String username, String rawPassword)
    {
        Objects.requireNonNull(username);
        Objects.requireNonNull(rawPassword);
        String storedPassword = db.oneString("SELECT user_pass WHERE user_name = ?", username);
        try
        {
            return passwordEncoder.check(rawPassword.toCharArray(), storedPassword);
        }
        catch (Exception e)
        {
            throw Be5Exception.internal(e);
        }
    }

    @Override
    public String finalPassword(String password)
    {
        Objects.requireNonNull(password);
        try
        {
            return passwordEncoder.encode(password.toCharArray());
        }
        catch (Exception e)
        {
            throw Be5Exception.internal(e);
        }
    }
}
