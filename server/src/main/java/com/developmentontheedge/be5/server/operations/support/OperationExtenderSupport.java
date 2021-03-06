package com.developmentontheedge.be5.server.operations.support;

import com.developmentontheedge.be5.security.UserInfo;
import com.developmentontheedge.be5.meta.Meta;
import com.developmentontheedge.be5.meta.UserAwareMeta;
import com.developmentontheedge.be5.database.DbService;
import com.developmentontheedge.be5.databasemodel.DatabaseModel;
import com.developmentontheedge.be5.operation.services.OperationBuilder;
import com.developmentontheedge.be5.operation.validation.Validator;
import com.developmentontheedge.be5.operation.support.BaseOperationExtenderSupport;
import com.developmentontheedge.be5.query.services.QueriesService;
import com.developmentontheedge.be5.server.services.DpsHelper;
import com.developmentontheedge.be5.web.Request;
import com.developmentontheedge.be5.web.Session;
import com.developmentontheedge.be5.web.impl.FileUploadWrapper;
import org.apache.commons.fileupload.FileItem;

import javax.inject.Inject;


public abstract class OperationExtenderSupport extends BaseOperationExtenderSupport
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
                       QueriesService queries, Session session, Request request, UserInfo userInfo)
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
        this.request = request;
        this.userInfo = userInfo;
    }

    protected FileItem getFileItem(String fileName)
    {
        FileUploadWrapper fileUploadWrapper = (FileUploadWrapper) request.getRawRequest();
        return fileUploadWrapper.getFileItem(fileName);
    }
}
