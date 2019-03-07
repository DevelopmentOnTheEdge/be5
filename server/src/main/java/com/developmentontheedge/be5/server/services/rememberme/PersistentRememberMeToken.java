package com.developmentontheedge.be5.server.services.rememberme;

import java.util.Date;

public class PersistentRememberMeToken
{
    private final String username;
    private final String series;
    private final String tokenValue;
    private final Date date;

    public PersistentRememberMeToken(String username, String series, String tokenValue,
                                     Date date)
    {
        this.username = username;
        this.series = series;
        this.tokenValue = tokenValue;
        this.date = date;
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

    public Date getDate()
    {
        return date;
    }
}
