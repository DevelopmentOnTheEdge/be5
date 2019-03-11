package com.developmentontheedge.be5.server.authentication.rememberme;

import java.sql.Timestamp;

public class PersistentRememberMeToken
{
    private final String username;
    private final String series;
    private final String tokenValue;
    private final Timestamp timestamp;

    public PersistentRememberMeToken(String username, String series, String tokenValue,
                                     Timestamp timestamp)
    {
        this.username = username;
        this.series = series;
        this.tokenValue = tokenValue;
        this.timestamp = timestamp;
    }

    public String getUsername()
    {
        return username;
    }

    public String getSeries()
    {
        return series;
    }

    public String getTokenValue()
    {
        return tokenValue;
    }

    public Timestamp getTimestamp()
    {
        return timestamp;
    }
}
