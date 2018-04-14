package com.developmentontheedge.be5.operation;

import com.developmentontheedge.be5.api.services.QRecService;
import com.developmentontheedge.be5.api.services.SqlService;
import com.developmentontheedge.be5.api.validation.Validator;
import com.developmentontheedge.be5.databasemodel.impl.DatabaseModel;
import com.developmentontheedge.be5.env.Inject;


public class OperationExtenderSupport implements OperationExtender
{
    @Inject public DatabaseModel database;
    @Inject public SqlService db;
    @Inject public QRecService qRec;
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
