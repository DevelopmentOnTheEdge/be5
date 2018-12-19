package com.developmentontheedge.be5.query.services.impl;

import com.developmentontheedge.be5.base.services.Meta;
import com.developmentontheedge.be5.base.services.UserInfoProvider;
import com.developmentontheedge.be5.database.DbService;
import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.query.QuerySession;
import com.developmentontheedge.be5.query.impl.Be5QueryContext;
import com.developmentontheedge.be5.query.impl.Be5QueryExecutor;
import com.developmentontheedge.be5.query.impl.QueryMetaHelper;
import com.developmentontheedge.be5.query.services.QueryService;
import com.developmentontheedge.sql.format.QueryContext;

import javax.inject.Inject;
import java.util.Map;


public class QueryServiceImpl implements QueryService
{
    private final Meta meta;
    private final DbService db;
    private final QuerySession querySession;
    private final UserInfoProvider userInfoProvider;
    private final QueryMetaHelper queryMetaHelper;

    @Inject
    public QueryServiceImpl(Meta meta, DbService db, QuerySession querySession,
                            UserInfoProvider userInfoProvider, QueryMetaHelper queryMetaHelper)
    {
        this.meta = meta;
        this.db = db;
        this.querySession = querySession;
        this.userInfoProvider = userInfoProvider;
        this.queryMetaHelper = queryMetaHelper;
    }

    @Override
    public Be5QueryExecutor build(Query query, Map<String, ?> parameters)
    {
        QueryContext context = new Be5QueryContext(query, parameters, querySession, userInfoProvider.get(), meta);
        return build(query, context);
    }

    @Override
    public Be5QueryExecutor build(Query query, QueryContext queryContext)
    {
        return new Be5QueryExecutor(query, queryContext, meta, db, queryMetaHelper);
    }
}
