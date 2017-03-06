package com.developmentontheedge.be5.legacy;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

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
    
    public Optional<LegacyQuery> findOne(int id)
    {
        return db.from(MetaTables.QUERIES).findOneWith(MetaTables.Queries.ID, id, this::parse);
    }
    
    private LegacyQuery parse(ResultSet rs) throws SQLException
    {
        return new LegacyQuery(rs.getString(MetaTables.Queries.ENTITY_NAME), rs.getString(MetaTables.Queries.NAME));
    }
    
}
