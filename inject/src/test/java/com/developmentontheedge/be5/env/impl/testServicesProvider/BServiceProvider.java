package com.developmentontheedge.be5.env.impl.testServicesProvider;

import com.developmentontheedge.be5.env.services.TestService;

import javax.inject.Provider;

public class BServiceProvider
{
    private final Provider<AServiceProvider> aService;
    private final TestService testService;

    public BServiceProvider(Provider<AServiceProvider> aService, TestService testService)
    {
        this.aService = aService;
        this.testService = testService;
    }

    public void bMethod()
    {
        testService.call("bMethod");
    }

    public void bMethodUseAService()
    {
        aService.get().aMethod();
    }
}
