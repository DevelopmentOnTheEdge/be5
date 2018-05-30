package com.developmentontheedge.be5.server.services.impl;

import com.developmentontheedge.be5.base.services.Meta;
import com.developmentontheedge.be5.database.DbService;
import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.query.QuerySession;
import com.developmentontheedge.be5.query.impl.Be5QueryExecutor;
import com.developmentontheedge.be5.query.services.QueryService;
import com.developmentontheedge.be5.server.servlet.UserInfoHolder;
import com.developmentontheedge.be5.web.Session;

import javax.inject.Inject;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class QueryServiceImpl implements QueryService
{
    private final Meta meta;
    private final DbService db;

    @Inject
    public QueryServiceImpl(Meta meta, DbService db)
    {
        this.meta = meta;
        this.db = db;
    }

    @Override
    public Be5QueryExecutor build(Query query, Map<String, ?> parameters)
    {
        Map<String, List<String>> listParams = getMapOfList(parameters);

        return new Be5QueryExecutor(query, listParams, UserInfoHolder.getUserInfo(),
                new SessionWrapper(UserInfoHolder.getSession()), meta, db);
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

    class SessionWrapper implements QuerySession
    {
        private final Session session;

        public SessionWrapper(Session session)
        {
            this.session = session;
        }

        @Override
        public Object get(String name)
        {
            return session.get(name);
        }
    }
}
