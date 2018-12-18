package com.developmentontheedge.be5.query.services.impl;

import com.developmentontheedge.be5.base.services.Meta;
import com.developmentontheedge.be5.base.services.UserAwareMeta;
import com.developmentontheedge.be5.base.services.UserInfoProvider;
import com.developmentontheedge.be5.database.DbService;
import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.query.QuerySession;
import com.developmentontheedge.be5.query.impl.Be5QueryExecutor;
import com.developmentontheedge.be5.query.services.QueryMetaHelper;
import com.developmentontheedge.be5.query.services.QueryService;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class QueryServiceImpl implements QueryService
{
    private final Meta meta;
    private final DbService db;
    private final UserAwareMeta userAwareMeta;
    private final Provider<QuerySession> querySession;
    private final UserInfoProvider userInfoProvider;
    private final QueryMetaHelper queryMetaHelper;

    @Inject
    public QueryServiceImpl(Meta meta, DbService db, UserAwareMeta userAwareMeta, Provider<QuerySession> querySession,
                            UserInfoProvider userInfoProvider, QueryMetaHelper queryMetaHelper)
    {
        this.meta = meta;
        this.db = db;
        this.userAwareMeta = userAwareMeta;
        this.querySession = querySession;
        this.userInfoProvider = userInfoProvider;
        this.queryMetaHelper = queryMetaHelper;
    }

    @Override
    public Be5QueryExecutor build(Query query, Map<String, ?> parameters)
    {
        Map<String, List<Object>> listParams = getMapOfList(parameters);

        return new Be5QueryExecutor(query, listParams, userInfoProvider.get(),
                querySession.get(), meta, userAwareMeta, db, queryMetaHelper);
    }

    @Override
    public Be5QueryExecutor build(Query query)
    {
        return build(query, Collections.emptyMap());
    }

    private Map<String, List<Object>> getMapOfList(Map<String, ?> parameters)
    {
        Map<String, List<Object>> mapOfList = new HashMap<>();
        parameters.forEach((k, v) -> mapOfList.put(k, getParameterList(v)));
        return mapOfList;
    }

    private static List<Object> getParameterList(Object value)
    {
        if (value == null) return null;

        if (value instanceof String[])
        {
            return Arrays.asList((String[]) value);
        }
        else if (value instanceof List)
        {
            @SuppressWarnings("unchecked")
            List<Object> list = (List<Object>) value;
            return list;
        }
        else
        {
            return Collections.singletonList(value.toString());
        }
    }
}
