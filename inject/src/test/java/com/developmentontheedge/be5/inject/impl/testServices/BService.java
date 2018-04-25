package com.developmentontheedge.be5.inject.impl.testServices;

import com.developmentontheedge.be5.inject.Inject;
import com.developmentontheedge.be5.inject.services.TestService;

public class BService
{
    @Inject private AService aService;

    private final TestService testService;

    public BService(TestService testService)
    {
        this.testService = testService;
    }

    public void bMethod()
    {
        testService.call("bMethod");
    }

    public void bMethodUseAService()
    {
        aService.aMethod();
    }
}
