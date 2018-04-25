package com.developmentontheedge.be5.inject.impl;

import com.developmentontheedge.be5.inject.Injector;
import com.developmentontheedge.be5.inject.Stage;
import com.developmentontheedge.be5.inject.impl.testServicesProvider.AServiceProvider;
import com.developmentontheedge.be5.inject.impl.testServicesProvider.BServiceProvider;
import com.developmentontheedge.be5.inject.services.impl.TestServiceMock;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.verify;


public class Be5InjectorCyclicDependenciesWithProviderTest
{
    @Before
    public void before()
    {
        TestServiceMock.clearMock();
    }

    @Test
    public void injectWithAnnotatedAServiceFirst()
    {
        Injector injector = new Be5Injector(Stage.TEST, new YamlBinder());

        injector.get(AServiceProvider.class).aMethodUseBService();

        verify(TestServiceMock.mock).call("aMethod in constructor");
        verify(TestServiceMock.mock).call("bMethod");

        injector.get(BServiceProvider.class).bMethodUseAService();
        verify(TestServiceMock.mock).call("aMethod");
    }

    @Test
    public void injectWithAnnotatedBServiceFirst()
    {
        Injector sqlMockInjector2 = new Be5Injector(Stage.TEST, new YamlBinder());

        sqlMockInjector2.get(BServiceProvider.class).bMethodUseAService();
        verify(TestServiceMock.mock).call("aMethod");

        sqlMockInjector2.get(AServiceProvider.class).aMethodUseBService();
        verify(TestServiceMock.mock).call("aMethod in constructor");
        verify(TestServiceMock.mock).call("bMethod");
    }

}