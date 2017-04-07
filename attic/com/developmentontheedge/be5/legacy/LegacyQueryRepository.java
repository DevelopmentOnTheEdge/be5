package com.developmentontheedge.be5.legacy;

import com.developmentontheedge.be5.api.services.SqlService;

/**
 * Fetches queries from the table.
 * 
 * @author asko
 */
public class LegacyQueryRepository
{

    private final SqlService db;

    public LegacyQueryRepository(SqlService db)
    {
        this.db = db;
    }
    
    public LegacyQuery findOne(int id)
    {
        return db.select("select * from queries WHERE ID = ?", LegacyQuery.parser, id);
    }

}
