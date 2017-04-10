package com.developmentontheedge.be5.legacy;

import com.developmentontheedge.be5.api.services.QueryLink;
import com.developmentontheedge.be5.api.sql.ResultSetParser;

/**
 * Represents a row in the queries table.
 * 
 * @author asko
 */
public class LegacyQuery
{
    public static final ResultSetParser<LegacyQuery> parser = rs ->
            new LegacyQuery(rs.getString(MetaTables.Queries.ENTITY_NAME), rs.getString(MetaTables.Queries.NAME));

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
