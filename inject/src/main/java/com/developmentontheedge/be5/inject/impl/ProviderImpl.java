package com.developmentontheedge.be5.inject.impl;

import com.developmentontheedge.be5.inject.Injector;

import javax.inject.Provider;


public class ProviderImpl<T> implements Provider
{
    private final Injector injector;
    private final Class<T> provideClass;

    ProviderImpl(Injector injector, Class<T> provideClass)
    {
        this.injector = injector;
        this.provideClass = provideClass;
    }

    @Override
    public T get()
    {
        return injector.get(provideClass);
    }
}
