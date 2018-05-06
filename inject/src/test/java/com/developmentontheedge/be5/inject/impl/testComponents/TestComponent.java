package com.developmentontheedge.be5.inject.impl.testComponents;

import javax.inject.Inject;
import com.developmentontheedge.be5.inject.services.TestService;


public class TestComponent
{
    @Inject private TestService testService;

    public void generate()
    {
        testService.call("component");
    }
}
