package com.developmentontheedge.be5.api;

import com.developmentontheedge.be5.api.helpers.UserHelper;
import com.developmentontheedge.be5.api.services.Meta;
import com.developmentontheedge.be5.api.services.ProjectProvider;
import com.developmentontheedge.be5.api.services.impl.MetaImpl;
import com.developmentontheedge.be5.api.services.impl.ProjectProviderImpl;
import com.google.inject.AbstractModule;
import com.google.inject.Scopes;


public class ServerModule extends AbstractModule
{
    @Override
    protected void configure()
    {
        bind(UserHelper.class).in(Scopes.SINGLETON);
        bind(Meta.class).to(MetaImpl.class).in(Scopes.SINGLETON);
        bind(ProjectProvider.class).to(ProjectProviderImpl.class).in(Scopes.SINGLETON);
    }
}
