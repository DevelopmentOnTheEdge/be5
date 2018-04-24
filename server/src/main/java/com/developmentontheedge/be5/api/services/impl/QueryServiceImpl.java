package com.developmentontheedge.be5.api.services.impl;

import com.developmentontheedge.be5.api.services.DatabaseService;
import com.developmentontheedge.be5.api.services.Meta;
import com.developmentontheedge.be5.api.services.QueryService;
import com.developmentontheedge.be5.api.services.SqlService;
import com.developmentontheedge.be5.query.impl.Be5QueryExecutor;
import com.developmentontheedge.be5.databasemodel.impl.DatabaseModel;
import com.developmentontheedge.be5.metadata.model.Query;

import javax.inject.Provider;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class QueryServiceImpl implements QueryService
{
    private final DatabaseService databaseService;
    private final Provider<DatabaseModel> database;
    private final Meta meta;
    private final SqlService db;

    public QueryServiceImpl(DatabaseService databaseService, Provider<DatabaseModel> database,
                            Meta meta, SqlService db)
    {
        this.databaseService = databaseService;
        this.database = database;
        this.meta = meta;
        this.db = db;
    }

    @Override
    public Be5QueryExecutor build(Query query, Map<String, ?> parameters)
    {
        Map<String, List<String>> listParams = new HashMap<>();
        parameters.forEach((k,v) -> listParams.put(k, getParameterList(v)));

        return new Be5QueryExecutor(query, listParams, databaseService, database, meta, db);
    }

    @Override
    public Be5QueryExecutor build(Query query)
    {
        return build(query, Collections.emptyMap());
    }

    private List<String> getParameterList(Object parameter)
    {
        if(parameter.getClass() == String.class)
        {
            return Collections.singletonList((String) parameter);
        }
        else
        {
            return (List<String>) parameter;
        }
    }

}
