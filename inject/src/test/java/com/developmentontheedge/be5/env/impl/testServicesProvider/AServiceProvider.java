package com.developmentontheedge.be5.env.impl.testServicesProvider;


import com.developmentontheedge.be5.env.services.TestService;

public class AServiceProvider
{
    private final BServiceProvider bService;
    private final TestService testService;

    public AServiceProvider(BServiceProvider bService, TestService testService)
    {
        this.bService = bService;
        this.testService = testService;

        testService.call("aMethod in constructor");
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
