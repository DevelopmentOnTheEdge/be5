package com.developmentontheedge.be5.env.impl.testServices;


import com.developmentontheedge.be5.api.services.SqlService;

public class AService
{
    private BService bService;
    private SqlService db;

    public AService(BService bService, SqlService db)
    {
        this.bService = bService;
        this.db = db;
    }

    public void aMethod()
    {
        db.update("aMethod sql");
    }

    public void aMethodUseBService()
    {
        bService.bMethod();
    }
}
