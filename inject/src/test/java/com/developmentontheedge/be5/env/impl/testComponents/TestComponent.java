package com.developmentontheedge.be5.env.impl.testComponents;

import com.developmentontheedge.be5.env.Inject;
import com.developmentontheedge.be5.env.services.TestService;


public class TestComponent
{
    @Inject private TestService testService;

    public void generate()
    {
        testService.call("component");
    }
}
