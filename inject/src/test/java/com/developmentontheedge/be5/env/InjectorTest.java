package com.developmentontheedge.be5.env;

import com.developmentontheedge.be5.env.impl.Be5Injector;
import com.developmentontheedge.be5.env.impl.YamlBinder;
import com.developmentontheedge.be5.env.impl.testComponents.TestComponent;
import com.developmentontheedge.be5.env.impl.testServices.AService;
import com.developmentontheedge.be5.env.impl.testServices.BService;
import com.developmentontheedge.be5.env.services.ConfigurableService;
import com.developmentontheedge.be5.env.services.TestService;
import com.developmentontheedge.be5.env.services.impl.TestServiceMock;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.verify;


public class InjectorTest
{
    @Before
    public void before()
    {
        TestServiceMock.clearMock();
    }

    @Test
    public void test()
    {
        Injector injector = new Be5Injector(Stage.TEST, new YamlBinder());

        assertEquals(TestComponent.class, injector.getComponent("testComponent").getClass());

        assertEquals(TestServiceMock.class, injector.get(TestService.class).getClass());
    }

    @Test
    public void testConfigurable()
    {
        Injector injector = new Be5Injector(Stage.TEST, new YamlBinder());

        injector.get(ConfigurableService.class);

        verify(TestServiceMock.mock).call("test.url 5");
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

}