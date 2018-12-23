package com.developmentontheedge.be5.server.queries.support;

import com.developmentontheedge.be5.base.model.UserInfo;
import com.developmentontheedge.be5.base.services.Meta;
import com.developmentontheedge.be5.database.DbService;
import com.developmentontheedge.be5.databasemodel.DatabaseModel;
import com.developmentontheedge.be5.operation.services.validation.Validator;
import com.developmentontheedge.be5.query.services.QueriesService;
import com.developmentontheedge.be5.query.support.BaseQueryExecutorSupport;
import com.developmentontheedge.be5.server.helpers.DpsHelper;
import com.developmentontheedge.be5.web.Request;
import com.developmentontheedge.be5.web.Session;

import javax.inject.Inject;


public abstract class QueryExecutorSupport extends BaseQueryExecutorSupport
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
    @Inject
    protected UserInfo userInfo;
}
