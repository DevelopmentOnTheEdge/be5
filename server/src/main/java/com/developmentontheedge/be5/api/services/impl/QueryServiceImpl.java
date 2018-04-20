package com.developmentontheedge.be5.api.services.impl;

import com.developmentontheedge.be5.api.helpers.FilterHelper;
import com.developmentontheedge.be5.api.services.DatabaseService;
import com.developmentontheedge.be5.api.services.Meta;
import com.developmentontheedge.be5.api.services.QueryService;
import com.developmentontheedge.be5.api.services.SqlService;
import com.developmentontheedge.be5.databasemodel.impl.DatabaseModel;
import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.query.impl.model.Be5QueryExecutor;

import java.util.Collections;
import java.util.Map;


public class QueryServiceImpl implements QueryService
{
    private final DatabaseService databaseService;
    private final DatabaseModel database;
    private final Meta meta;
    private final SqlService db;
    private final FilterHelper filterHelper;

    public QueryServiceImpl(DatabaseService databaseService, DatabaseModel database,
                            Meta meta, SqlService db, FilterHelper filterHelper)
    {
        this.databaseService = databaseService;
        this.database = database;
        this.meta = meta;
        this.db = db;
        this.filterHelper = filterHelper;
    }

    @Override
    public Be5QueryExecutor build(Query query, Map<String, String> parameters)
    {
        return new Be5QueryExecutor(query, parameters, databaseService, database, meta, db, filterHelper);
    }

    @Override
    public Be5QueryExecutor build(Query query)
    {
        return build(query, Collections.emptyMap());
    }
}
