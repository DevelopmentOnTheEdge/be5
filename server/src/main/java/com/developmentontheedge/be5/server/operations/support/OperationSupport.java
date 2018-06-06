package com.developmentontheedge.be5.server.operations.support;

import com.developmentontheedge.be5.base.FrontendConstants;
import com.developmentontheedge.be5.base.model.UserInfo;
import com.developmentontheedge.be5.base.services.Meta;
import com.developmentontheedge.be5.base.util.HashUrl;
import com.developmentontheedge.be5.database.DbService;
import com.developmentontheedge.be5.databasemodel.DatabaseModel;
import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.operation.model.Operation;
import com.developmentontheedge.be5.operation.model.OperationContext;
import com.developmentontheedge.be5.operation.model.OperationInfo;
import com.developmentontheedge.be5.operation.model.OperationResult;
import com.developmentontheedge.be5.operation.services.OperationsFactory;
import com.developmentontheedge.be5.operation.services.validation.Validator;
import com.developmentontheedge.be5.operation.support.BaseOperationSupport;
import com.developmentontheedge.be5.query.services.QueriesService;
import com.developmentontheedge.be5.server.RestApiConstants;
import com.developmentontheedge.be5.server.helpers.DpsHelper;
import com.developmentontheedge.be5.server.util.HashUrlUtils;
import com.developmentontheedge.be5.web.Request;
import com.developmentontheedge.be5.web.Session;
import com.developmentontheedge.be5.web.SessionConstants;

import javax.inject.Inject;
import java.util.Map;


public abstract class OperationSupport extends BaseOperationSupport implements Operation
{
    @Inject public Meta meta;
    @Inject public DbService db;
    @Inject public DatabaseModel database;
    @Inject public DpsHelper dpsHelper;
    @Inject public Validator validator;
    @Inject public OperationsFactory operations;
    @Inject public QueriesService queries;

    @Inject protected Session session;
    @Inject protected Request request;

    protected UserInfo userInfo;

    @Override
    public final void initialize(OperationInfo info, OperationContext context, OperationResult operationResult)
    {
        super.initialize(info, context, operationResult);

        this.userInfo = (UserInfo) session.get(SessionConstants.USER_INFO);
    }

    public Query getQuery()
    {
        return meta.getQuery(getInfo().getEntityName(), context.getQueryName());
    }

    public void redirectThisOperation()
    {
        setResult(OperationResult.redirect(HashUrlUtils.getUrl(this).toString()));
    }

    public void redirectThisOperationNewId(Object newID)
    {
        setResult(OperationResult.redirect(getUrlForNewRecordId(newID).toString()));
    }

    public void redirectToTable(String entityName, String queryName, Map<String, Object> params)
    {
        setResult(OperationResult.redirect(new HashUrl(FrontendConstants.TABLE_ACTION, entityName, queryName).named(params).toString()));
    }

    public void redirectToTable(Query query, Map<String, Object> params)
    {
        setResult(OperationResult.redirect(new HashUrl(FrontendConstants.TABLE_ACTION, query.getEntity().getName(), query.getName()).named(params).toString()));
    }

    public HashUrl getUrlForNewRecordId(Object newID)
    {
        return new HashUrl(FrontendConstants.FORM_ACTION, getInfo().getEntityName(), context.getQueryName(), getInfo().getName())
                .named(getRedirectParams())
                .named(RestApiConstants.SELECTED_ROWS, newID.toString());
    }

}
