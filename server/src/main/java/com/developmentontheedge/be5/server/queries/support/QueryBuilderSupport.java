package com.developmentontheedge.be5.server.queries.support;

import com.developmentontheedge.be5.base.model.UserInfo;
import com.developmentontheedge.be5.base.services.Meta;
import com.developmentontheedge.be5.database.DbService;
import com.developmentontheedge.be5.databasemodel.DatabaseModel;
import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.operation.services.validation.Validator;
import com.developmentontheedge.be5.query.QueryExecutor;
import com.developmentontheedge.be5.query.services.QueriesService;
import com.developmentontheedge.be5.query.support.BaseQueryBuilderSupport;
import com.developmentontheedge.be5.server.SessionConstants;
import com.developmentontheedge.be5.server.helpers.DpsHelper;
import com.developmentontheedge.be5.web.Request;
import com.developmentontheedge.be5.web.Session;

import javax.inject.Inject;
import java.util.Map;


public abstract class QueryBuilderSupport extends BaseQueryBuilderSupport
{
    @Inject
    public DatabaseModel database;
    @Inject
    public DbService db;
    @Inject
    public DpsHelper dpsHelper;
    @Inject
    public Meta meta;
    @Inject
    public QueriesService queries;
    @Inject
    public Validator validator;

    @Inject
    protected Request request;
    @Inject
    protected Session session;

    protected UserInfo userInfo;

    public QueryExecutor initialize(Query query, Map<String, Object> parameters)
    {
        super.initialize(query, parameters);

        this.userInfo = (UserInfo) session.get(SessionConstants.USER_INFO);

        return this;
    }
}
