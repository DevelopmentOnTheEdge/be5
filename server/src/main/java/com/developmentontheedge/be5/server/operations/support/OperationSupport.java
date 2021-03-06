package com.developmentontheedge.be5.server.operations.support;

import com.developmentontheedge.be5.FrontendConstants;
import com.developmentontheedge.be5.database.DbService;
import com.developmentontheedge.be5.databasemodel.DatabaseModel;
import com.developmentontheedge.be5.meta.Meta;
import com.developmentontheedge.be5.meta.UserAwareMeta;
import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.operation.Operation;
import com.developmentontheedge.be5.operation.OperationResult;
import com.developmentontheedge.be5.operation.services.OperationBuilder;
import com.developmentontheedge.be5.operation.support.BaseOperationSupport;
import com.developmentontheedge.be5.operation.validation.Validator;
import com.developmentontheedge.be5.query.services.QueriesService;
import com.developmentontheedge.be5.security.UserInfo;
import com.developmentontheedge.be5.server.FrontendActions;
import com.developmentontheedge.be5.server.services.DpsHelper;
import com.developmentontheedge.be5.server.model.FrontendAction;
import com.developmentontheedge.be5.util.HashUrl;
import com.developmentontheedge.be5.util.JsonUtils;
import com.developmentontheedge.be5.web.Request;
import com.developmentontheedge.be5.web.Session;
import com.developmentontheedge.be5.web.impl.FileUploadWrapper;

import org.apache.commons.fileupload.FileItem;

import javax.inject.Inject;

import static com.developmentontheedge.be5.server.FrontendActions.redirect;
import static com.developmentontheedge.be5.server.FrontendActions.successAlert;

import java.util.Map;


public abstract class OperationSupport extends BaseOperationSupport implements Operation
{
    protected Meta meta;
    protected UserAwareMeta userAwareMeta;
    protected DbService db;
    protected DatabaseModel database;
    protected DpsHelper dpsHelper;
    protected Validator validator;
    protected OperationBuilder.OperationsFactory operations;
    protected QueriesService queries;

    protected Session session;
    protected Request request;
    protected UserInfo userInfo;

    @Inject
    protected void inject(Meta meta, UserAwareMeta userAwareMeta, DbService db, DatabaseModel database,
                       DpsHelper dpsHelper, Validator validator, OperationBuilder.OperationsFactory operations,
                       QueriesService queries, Session session, /*Request request,*/ UserInfo userInfo)
    {
        this.meta = meta;
        this.userAwareMeta = userAwareMeta;
        this.db = db;
        this.database = database;
        this.dpsHelper = dpsHelper;
        this.validator = validator;
        this.operations = operations;
        this.queries = queries;
        this.session = session;
        //this.request = request;
        this.userInfo = userInfo;
    }

    public void setRequest( Request request )
    {
        this.request = request;
    }

    protected Query getQuery()
    {
        return meta.getQuery(getInfo().getEntityName(), context.getQueryName());
    }

    protected void setResultFinished()
    {
        setResult(OperationResult.finished());
    }

    protected void setResultFinished(String message)
    {
        setResult(OperationResult.finished(message));
    }

    protected void setResultFinished(String message, FrontendAction... frontendActions)
    {
        setResult(OperationResult.finished(message, frontendActions));
    }

    protected void setResultFinished(FrontendAction... frontendActions)
    {
        setResult(OperationResult.finished(null, frontendActions));
    }

    public void redirectThisOperation()
    {
        String url = new HashUrl(FrontendConstants.FORM_ACTION,
                info.getEntityName(), context.getQueryName(), info.getName())
                .named(getRedirectParams()).toString();
        setResultFinished(
                successAlert(userAwareMeta.getLocalizedInfoMessage("Successfully completed.")),
                redirect(url)
        );
    }

    @Override
    public void setResultGoBack()
    {
        setResultFinished(FrontendActions.goBackOrRedirect(getBackUrl()));
    }

    protected FileItem getFileItem(String fileName)
    {
        FileUploadWrapper fileUploadWrapper = (FileUploadWrapper) request.getRawRequest();
        return fileUploadWrapper.getFileItem(fileName);
    }

    public boolean isModalFormLayout()
    {
        Map<String, Object> layout = JsonUtils.getMapFromJson(getInfo().getModel().getLayout());
        return "modalForm".equals(layout.get("type"));
    }
}
