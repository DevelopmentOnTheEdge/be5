package com.developmentontheedge.be5.env.impl.testServices;


import com.developmentontheedge.be5.env.services.TestService;

public class AService
{
    private final BService bService;
    private final TestService testService;

    public AService(BService bService, TestService testService)
    {
        this.bService = bService;
        this.testService = testService;
    }

    public void aMethod()
    {
        testService.call("aMethod");
    }

    public void aMethodUseBService()
    {
        bService.bMethod();
    }
}
