package com.developmentontheedge.be5.test.mocks;

import com.developmentontheedge.be5.server.services.rememberme.PersistentRememberMeToken;
import com.developmentontheedge.be5.server.services.rememberme.PersistentTokenRepository;

import java.util.Date;

public class RememberUserHelperMock implements PersistentTokenRepository
{
    @Override
    public void createNewToken(PersistentRememberMeToken token)
    {

    }

    @Override
    public void updateToken(String series, String tokenValue, Date lastUsed)
    {

    }

    @Override
    public PersistentRememberMeToken getTokenForSeries(String seriesId)
    {
        return null;
    }

    @Override
    public void removeUserTokens(String username)
    {

    }
}
