package com.developmentontheedge.be5.server.operations.support;

import com.developmentontheedge.be5.base.services.Meta;
import com.developmentontheedge.be5.database.DbService;
import com.developmentontheedge.be5.databasemodel.DatabaseModel;
import com.developmentontheedge.be5.operation.model.Operation;
import com.developmentontheedge.be5.operation.model.OperationExtender;
import com.developmentontheedge.be5.operation.services.OperationsFactory;
import com.developmentontheedge.be5.operation.services.validation.Validator;
import com.developmentontheedge.be5.query.services.QueriesService;
import com.developmentontheedge.be5.server.helpers.DpsHelper;

import javax.inject.Inject;


public abstract class OperationExtenderSupport implements OperationExtender
{
    @Inject public Meta meta;
    @Inject public DbService db;
    @Inject public DatabaseModel database;
    @Inject public DpsHelper dpsHelper;
    @Inject public Validator validator;
    @Inject public OperationsFactory operations;
    @Inject public QueriesService queries;

    @Override
    public boolean skipInvoke(Operation op, Object parameters)
    {
        return false;
    }

    @Override
    public void preInvoke(Operation op, Object parameters) throws Exception
    {

    }

    @Override
    public void postInvoke(Operation op, Object parameters) throws Exception
    {

    }
}
