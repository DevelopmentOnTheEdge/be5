package com.developmentontheedge.be5.server.queries.support;

import com.developmentontheedge.be5.base.model.UserInfo;
import com.developmentontheedge.be5.base.services.Meta;
import com.developmentontheedge.be5.database.DbService;
import com.developmentontheedge.be5.databasemodel.DatabaseModel;
import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.operation.services.validation.Validator;
import com.developmentontheedge.be5.query.TableBuilder;
import com.developmentontheedge.be5.query.support.BaseTableBuilderSupport;
import com.developmentontheedge.be5.server.helpers.DpsHelper;
import com.developmentontheedge.be5.server.helpers.OperationHelper;
import com.developmentontheedge.be5.server.servlet.UserInfoHolder;
import com.developmentontheedge.be5.web.Request;
import com.developmentontheedge.be5.web.Session;

import javax.inject.Inject;
import java.util.Map;


public abstract class TableBuilderSupport extends BaseTableBuilderSupport
{
    @Inject public DatabaseModel database;
    @Inject public DbService db;
    @Inject public DpsHelper dpsHelper;
    @Inject public Meta meta;
    @Inject public OperationHelper helper;
    @Inject public Validator validator;

    protected Request request;
    protected Session session;
    protected UserInfo userInfo;

    public TableBuilder initialize(Query query, Map<String, Object> parameters)
    {
        super.initialize(query, parameters);

        this.request = UserInfoHolder.getRequest();
        this.session = UserInfoHolder.getSession();
        this.userInfo = UserInfoHolder.getUserInfo();

        return this;
    }

}