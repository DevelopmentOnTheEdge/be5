package com.developmentontheedge.be5.operation.support;

import com.developmentontheedge.be5.base.services.Meta;
import com.developmentontheedge.be5.database.DbService;
import com.developmentontheedge.be5.operation.services.validation.Validator;

import javax.inject.Inject;

public abstract class TestOperationSupport extends BaseOperationSupport
{
    @Inject
    public DbService db;
    @Inject
    public Meta meta;
    @Inject
    public Validator validator;
}
