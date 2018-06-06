package com.developmentontheedge.be5.operation.support;

import com.developmentontheedge.be5.database.DbService;

import javax.inject.Inject;

public abstract class TestOperationExtenderSupport extends BaseOperationExtenderSupport
{
    @Inject public DbService db;
}
