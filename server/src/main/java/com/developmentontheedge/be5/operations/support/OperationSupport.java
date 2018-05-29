package com.developmentontheedge.be5.operations.support;

import com.developmentontheedge.be5.api.Request;
import com.developmentontheedge.be5.api.RestApiConstants;
import com.developmentontheedge.be5.api.Session;
import com.developmentontheedge.be5.api.helpers.DpsHelper;
import com.developmentontheedge.be5.api.helpers.OperationHelper;
import com.developmentontheedge.be5.api.services.OperationsFactory;
import com.developmentontheedge.be5.operation.support.BaseOperationSupport;
import com.developmentontheedge.be5.servlet.UserInfoHolder;
import com.developmentontheedge.be5.api.services.Meta;
import com.developmentontheedge.be5.database.DbService;
import com.developmentontheedge.be5.api.services.validation.Validator;
import com.developmentontheedge.be5.api.FrontendConstants;
import com.developmentontheedge.be5.databasemodel.DatabaseModel;
import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.model.UserInfo;
import com.developmentontheedge.be5.operation.Operation;
import com.developmentontheedge.be5.operation.OperationContext;
import com.developmentontheedge.be5.operation.OperationInfo;
import com.developmentontheedge.be5.operation.OperationResult;
import com.developmentontheedge.be5.util.HashUrl;
import com.developmentontheedge.be5.util.HashUrlUtils;

import javax.inject.Inject;
import java.util.Map;


public abstract class OperationSupport extends BaseOperationSupport implements Operation
{
    @Inject public DatabaseModel database;
    @Inject public OperationsFactory operations;
    @Inject public DbService db;
    @Inject public DpsHelper dpsHelper;
    @Inject public Meta meta;
    @Inject public OperationHelper helper;
    @Inject public Validator validator;

    protected Request request;
    protected Session session;
    protected UserInfo userInfo;

    @Override
    public final void initialize(OperationInfo info, OperationContext context, OperationResult operationResult)
    {
        super.initialize(info, context, operationResult);

        this.request = UserInfoHolder.getRequest();
        this.session = UserInfoHolder.getSession();
        this.userInfo = UserInfoHolder.getUserInfo();
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
