package com.developmentontheedge.be5.env.impl.testServices;

import com.developmentontheedge.be5.api.services.SqlService;
import com.developmentontheedge.be5.env.Inject;

public class BService
{
    @Inject private AService aService;

    private SqlService db;

    public BService(SqlService db)
    {
        this.db = db;
    }

    public void bMethod()
    {
        db.update("bMethod sql");
    }

    public void bMethodUseAService()
    {
        aService.aMethod();
    }
}
