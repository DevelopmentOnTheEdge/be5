package com.developmentontheedge.be5.inject;

import com.developmentontheedge.be5.inject.impl.Be5Injector;
import com.developmentontheedge.be5.inject.impl.YamlBinder;
import com.developmentontheedge.be5.inject.impl.testComponents.TestComponent;
import com.developmentontheedge.be5.inject.services.ConfigurableService;
import com.developmentontheedge.be5.inject.services.TestService;
import com.developmentontheedge.be5.inject.services.impl.TestServiceMock;
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
        Injector injector = new Be5Injector(Stage.PRODUCTION, new YamlBinder());

        assertEquals(TestComponent.class, injector.getComponent("testComponent").getClass());

        assertEquals(TestServiceMock.class, injector.get(TestService.class).getClass());

        assertEquals(Stage.PRODUCTION, injector.get(Stage.class));

        assertEquals(Be5Injector.class, injector.get(Injector.class).getClass());
    }

    @Test
    public void testConfigurable()
    {
        Injector injector = new Be5Injector(new YamlBinder());

        injector.get(ConfigurableService.class);

        verify(TestServiceMock.mock).call("test.url 5");
    }

}