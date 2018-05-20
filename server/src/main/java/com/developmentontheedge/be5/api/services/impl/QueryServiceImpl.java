package com.developmentontheedge.be5.api.services.impl;

import com.developmentontheedge.be5.api.helpers.UserInfoHolder;
import com.developmentontheedge.be5.api.services.ConnectionService;
import com.developmentontheedge.be5.api.services.DatabaseService;
import com.developmentontheedge.be5.api.services.Meta;
import com.developmentontheedge.be5.api.services.QueryService;
import com.developmentontheedge.be5.api.services.SqlService;
import com.developmentontheedge.be5.query.impl.Be5QueryExecutor;
import com.developmentontheedge.be5.api.services.databasemodel.impl.DatabaseModel;
import com.developmentontheedge.be5.metadata.model.Query;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class QueryServiceImpl implements QueryService
{
    private final DatabaseService databaseService;
    private final ConnectionService connectionService;
    private final Provider<DatabaseModel> database;
    private final Meta meta;
    private final SqlService db;

    @Inject
    public QueryServiceImpl(DatabaseService databaseService, ConnectionService connectionService, Provider<DatabaseModel> database,
                            Meta meta, SqlService db)
    {
        this.databaseService = databaseService;
        this.connectionService = connectionService;
        this.database = database;
        this.meta = meta;
        this.db = db;
    }

    @Override
    public Be5QueryExecutor build(Query query, Map<String, ?> parameters)
    {
        Map<String, List<String>> listParams = getMapOfList(parameters);

        return new Be5QueryExecutor(query, listParams, UserInfoHolder.getUserInfo(), UserInfoHolder.getSession(),
                connectionService, databaseService, database, meta, db);
    }

    @Override
    public Be5QueryExecutor build(Query query)
    {
        return build(query, Collections.emptyMap());
    }

    private Map<String, List<String>> getMapOfList(Map<String, ?> parameters)
    {
        Map<String, List<String>> listParams = new HashMap<>();
        parameters.forEach((k,v) -> listParams.put(k, getParameterList(v)));

        return listParams;
    }

    @SuppressWarnings("unchecked")
    private List<String> getParameterList(Object parameter)
    {
        if(parameter == null)return null;

        if(parameter instanceof List)
        {
            return (List<String>) parameter;
        }
        else
        {
            return Collections.singletonList(parameter.toString());
        }
    }

}
