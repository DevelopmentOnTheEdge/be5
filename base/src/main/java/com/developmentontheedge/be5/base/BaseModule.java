package com.developmentontheedge.be5.base;

import com.developmentontheedge.be5.api.services.Be5Caches;
import com.developmentontheedge.be5.api.services.ConnectionService;
import com.developmentontheedge.be5.api.services.DataSourceService;
import com.developmentontheedge.be5.api.services.DbService;
import com.developmentontheedge.be5.api.services.GroovyRegister;
import com.developmentontheedge.be5.api.services.Meta;
import com.developmentontheedge.be5.api.services.ProjectProvider;
import com.developmentontheedge.be5.api.services.impl.Be5CachesImpl;
import com.developmentontheedge.be5.api.services.impl.ConnectionServiceImpl;
import com.developmentontheedge.be5.api.services.impl.DataSourceServiceImpl;
import com.developmentontheedge.be5.api.services.impl.DbServiceImpl;
import com.developmentontheedge.be5.api.services.impl.LogConfigurator;
import com.developmentontheedge.be5.api.services.impl.MetaImpl;
import com.developmentontheedge.be5.api.services.impl.ProjectProviderImpl;
import com.developmentontheedge.be5.api.services.impl.SqlHelper;
import com.google.inject.Scopes;
import com.google.inject.servlet.ServletModule;


public class BaseModule extends ServletModule
{
    @Override
    protected void configureServlets()
    {
        bind(LogConfigurator.class).asEagerSingleton();
        bind(GroovyRegister.class).in(Scopes.SINGLETON);
        bind(SqlHelper.class).in(Scopes.SINGLETON);

        bind(ProjectProvider.class).to(ProjectProviderImpl.class).in(Scopes.SINGLETON);
        bind(DataSourceService.class).to(DataSourceServiceImpl.class).in(Scopes.SINGLETON);
        bind(ConnectionService.class).to(ConnectionServiceImpl.class).in(Scopes.SINGLETON);
        bind(DbService.class).to(DbServiceImpl.class).in(Scopes.SINGLETON);
        bind(Meta.class).to(MetaImpl.class).in(Scopes.SINGLETON);
        bind(Be5Caches.class).to(Be5CachesImpl.class).in(Scopes.SINGLETON);
    }
}
