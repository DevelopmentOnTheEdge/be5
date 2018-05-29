package com.developmentontheedge.be5.operation.model;

import com.developmentontheedge.be5.api.services.GroovyOperationLoader;
import com.developmentontheedge.be5.api.services.OperationExecutor;
import com.developmentontheedge.be5.api.services.OperationsFactory;
import com.developmentontheedge.be5.api.services.impl.OperationExecutorImpl;
import com.developmentontheedge.be5.api.services.impl.OperationsFactoryImpl;
import com.developmentontheedge.be5.api.services.validation.Validator;
import com.google.inject.AbstractModule;
import com.google.inject.Scopes;


public class OperationModule extends AbstractModule
{
    @Override
    protected void configure()
    {
        bind(Validator.class).in(Scopes.SINGLETON);
        bind(GroovyOperationLoader.class).in(Scopes.SINGLETON);

        bind(OperationExecutor.class).to(OperationExecutorImpl.class).in(Scopes.SINGLETON);
        bind(OperationsFactory.class).to(OperationsFactoryImpl.class).in(Scopes.SINGLETON);
    }
}
