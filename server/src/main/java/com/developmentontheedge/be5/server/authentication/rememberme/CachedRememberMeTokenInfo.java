package com.developmentontheedge.be5.server.authentication.rememberme;

/**
 * Tokens used for linking information about user with information which stored in user's remember-me cookies.
 * All this information stores in database. Token value updates every time when be accessed.
 * We need this class to store "remember-me" token value and caching time in cache.
 *
 * @author Mikhail Stryzhonok
 * @see PersistentRememberMeToken
 * @see ThrottlingRememberMeService
 */
public class CachedRememberMeTokenInfo
{
    private String value;
    long cachingTime;

    public CachedRememberMeTokenInfo(String tokenValue, long cachingTime)
    {
        this.value = tokenValue;
        this.cachingTime = cachingTime;
    }

    /**
     * Gets token date and time of token caching as milliseconds
     *
     * @return Date and time of token caching
     */
    public long getCachingTime()
    {
        return cachingTime;
    }

    public String getValue()
    {
        return value;
    }
}
