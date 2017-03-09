package com.developmentontheedge.be5.metadata.caches;

public class SystemSettingsCache
{
    private SystemSettingsCache()
    {
    }

    private static Cache instance;

    public static Cache getInstance()
    {
        if( instance == null )
        {
            synchronized( SystemSettingsCache.class )
            { 
                if( instance == null )
                {
                    instance = CacheFactory.getCacheInstance( "systemSettingsCache" );
                }
            }
        }    
        return instance;
    }
}
