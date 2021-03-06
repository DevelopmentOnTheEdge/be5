package com.developmentontheedge.be5.operation;

import com.developmentontheedge.be5.operation.services.GroovyOperationLoader;
import com.developmentontheedge.be5.operation.services.OperationBuilder;
import com.developmentontheedge.be5.operation.services.OperationExecutor;
import com.developmentontheedge.be5.operation.services.OperationService;
import com.developmentontheedge.be5.operation.services.impl.OperationExecutorImpl;
import com.developmentontheedge.be5.operation.services.impl.OperationServiceImpl;
import com.developmentontheedge.be5.operation.validation.Validator;
import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.assistedinject.FactoryModuleBuilder;


public class OperationModule extends AbstractModule
{
    @Override
    protected void configure()
    {
        bind(Validator.class).in(Scopes.SINGLETON);
        bind(GroovyOperationLoader.class).in(Scopes.SINGLETON);

        bind(OperationExecutor.class).to(OperationExecutorImpl.class).in(Scopes.SINGLETON);
        bind(OperationService.class).to(OperationServiceImpl.class).in(Scopes.SINGLETON);
        install(new FactoryModuleBuilder().implement(OperationBuilder.class, OperationBuilder.class)
                .build(OperationBuilder.OperationsFactory.class));
    }
}
