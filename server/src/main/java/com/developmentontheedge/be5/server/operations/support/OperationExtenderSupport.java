package com.developmentontheedge.be5.server.operations.support;

import com.developmentontheedge.be5.database.DbService;
import com.developmentontheedge.be5.operation.model.Operation;
import com.developmentontheedge.be5.operation.model.OperationExtender;
import com.developmentontheedge.be5.operation.services.validation.Validator;

import javax.inject.Inject;


public abstract class OperationExtenderSupport implements OperationExtender
{
    @Inject public DbService db;
    @Inject public Validator validator;

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
