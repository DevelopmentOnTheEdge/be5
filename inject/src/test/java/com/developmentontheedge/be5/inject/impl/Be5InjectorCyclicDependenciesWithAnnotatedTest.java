package com.developmentontheedge.be5.inject.impl;

import com.developmentontheedge.be5.inject.Injector;
import com.developmentontheedge.be5.inject.Stage;
import com.developmentontheedge.be5.inject.impl.testServices.AService;
import com.developmentontheedge.be5.inject.impl.testServices.BService;
import com.developmentontheedge.be5.inject.services.impl.TestServiceMock;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.verify;


public class Be5InjectorCyclicDependenciesWithAnnotatedTest
{
    @Before
    public void before()
    {
        TestServiceMock.clearMock();
    }

    @Test
    public void injectWithAnnotatedAServiceFirst()
    {
        Injector injector = new Be5Injector(Stage.DEVELOPMENT, new YamlBinder());

        injector.get(AService.class).aMethodUseBService();
        verify(TestServiceMock.mock).call("bMethod");

        injector.get(BService.class).bMethodUseAService();
        verify(TestServiceMock.mock).call("aMethod");
    }

    @Test
    public void injectWithAnnotatedBServiceFirst()
    {
        Injector sqlMockInjector2 = new Be5Injector(Stage.DEVELOPMENT, new YamlBinder());

        sqlMockInjector2.get(BService.class).bMethodUseAService();
        verify(TestServiceMock.mock).call("aMethod");

        sqlMockInjector2.get(AService.class).aMethodUseBService();
        verify(TestServiceMock.mock).call("bMethod");
    }

}