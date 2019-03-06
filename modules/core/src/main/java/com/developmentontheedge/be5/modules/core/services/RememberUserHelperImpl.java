package com.developmentontheedge.be5.modules.core.services;

import com.developmentontheedge.be5.server.helpers.RememberUserHelper;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

public class RememberUserHelperImpl implements RememberUserHelper
{
    private SecureRandom random = new SecureRandom();
    private Map<String, String> rememberedUsers = new HashMap<>();

    @Override
    public String rememberUser(String username)
    {
        String randomId = new BigInteger(130, random).toString(32);
        rememberedUsers.put(randomId, username);
        return randomId;
    }

    @Override
    public String getRememberedUser(String id)
    {
        return rememberedUsers.get(id);
    }

    @Override
    public void removeRememberedUser(String id)
    {
        rememberedUsers.remove(id);
    }
}
