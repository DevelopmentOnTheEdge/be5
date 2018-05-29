package com.developmentontheedge.be5.operations.support;

import com.developmentontheedge.be5.database.DbService;
import com.developmentontheedge.be5.api.services.validation.Validator;
import com.developmentontheedge.be5.databasemodel.DatabaseModel;
import javax.inject.Inject;
import com.developmentontheedge.be5.operation.model.Operation;
import com.developmentontheedge.be5.operation.model.OperationExtender;


public abstract class OperationExtenderSupport implements OperationExtender
{
    @Inject public DatabaseModel database;
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
