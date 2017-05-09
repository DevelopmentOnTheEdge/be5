package com.developmentontheedge.be5.api.operationstest.v1;

import com.developmentontheedge.be5.util.HashUrl;

public class LegacyUrlsService
{
    
    /**
     * Creates a low-level API item to parse legacy URLs.
     */
    public LegacyUrlParser createParser(String value)
    {
        return new LegacyUrlParser(value);
    }
    
    /**
     * Transforms a legacy URL. Should be used when URLs are knowingly legacy.
     */
    public HashUrl modernize(String value)
    {
        return createParser(value).modernize();
    }
    
}
