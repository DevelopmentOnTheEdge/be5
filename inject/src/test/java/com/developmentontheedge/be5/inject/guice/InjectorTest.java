package com.developmentontheedge.be5.inject.guice;

import com.developmentontheedge.be5.inject.impl.testComponents.TestComponent;
import com.developmentontheedge.be5.inject.services.TestService;
import com.developmentontheedge.be5.inject.services.impl.TestServiceMock;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Stage;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;


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
        Injector injector = Guice.createInjector(new TestModule());

        TestComponent component = injector.getInstance(TestComponent.class);

        assertEquals(TestComponent.class, component.getClass());

        assertEquals(TestServiceMock.class, injector.getInstance(TestService.class).getClass());

        assertEquals(Stage.DEVELOPMENT, injector.getInstance(Stage.class));
    }

    @Test
    public void testConfigurable()
    {
        Injector injector = Guice.createInjector(new TestModule());

//        injector.get(ConfigurableService.class);
//
//        verify(TestServiceMock.mock).call("test.url 5");
    }

}