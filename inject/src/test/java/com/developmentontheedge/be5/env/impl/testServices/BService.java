package com.developmentontheedge.be5.env.impl.testServices;

import com.developmentontheedge.be5.env.Inject;
import com.developmentontheedge.be5.env.services.TestService;

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
