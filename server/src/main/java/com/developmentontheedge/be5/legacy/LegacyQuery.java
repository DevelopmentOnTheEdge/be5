package com.developmentontheedge.be5.legacy;

import com.developmentontheedge.be5.api.services.QueryLink;

/**
 * Represents a row in the queries table.
 * 
 * @author asko
 */
public class LegacyQuery
{
    
    private final String entityName;
    private final String name;
    
    public LegacyQuery(String entityName, String name)
    {
        this.entityName = entityName;
        this.name = name;
    }
    
    public QueryLink toQueryLink()
    {
        return new QueryLink(entityName, name);
    }
    
}
