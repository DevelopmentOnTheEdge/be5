package com.developmentontheedge.be5.inject.guice;

import com.developmentontheedge.be5.inject.impl.testComponents.TestComponent;
import com.developmentontheedge.be5.inject.services.TestService;
import com.developmentontheedge.be5.inject.services.impl.TestServiceMock;
import com.google.inject.AbstractModule;


public class TestModule extends AbstractModule
{
    @Override
    protected void configure() {
        bind(TestService.class).to(TestServiceMock.class);

        bind(TestComponent.class);
        bind(ConfigurableService.class);

//        bindConstant().annotatedWith(Names.named("test-config"))
//                .to(new ConfigurableService.Config("test.url", 5));
    }
}
