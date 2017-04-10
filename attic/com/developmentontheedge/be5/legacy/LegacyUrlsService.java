package com.developmentontheedge.be5.legacy;

import com.developmentontheedge.be5.util.HashUrl;

public class LegacyUrlsService
{
    
    private final LegacyQueryRepository queryRepository;
    
    public LegacyUrlsService(LegacyQueryRepository queryRepository)
    {
        this.queryRepository = queryRepository;
    }
    
    /**
     * Creates a low-level API item to parse legacy URLs.
     */
    public LegacyUrlParser createParser(String value)
    {
        return new LegacyUrlParser(queryRepository, value);
    }
    
    /**
     * Transforms a legacy URL. Should be used when URLs are knowingly legacy.
     */
    public HashUrl modernize(String value)
    {
        return createParser(value).modernize();
    }
    
}
