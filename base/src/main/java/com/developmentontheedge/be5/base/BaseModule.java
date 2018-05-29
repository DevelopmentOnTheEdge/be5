package com.developmentontheedge.be5.base;

import com.developmentontheedge.be5.api.helpers.UserAwareMeta;
import com.developmentontheedge.be5.api.helpers.impl.UserAwareMetaImpl;
import com.developmentontheedge.be5.api.services.Be5Caches;
import com.developmentontheedge.be5.database.ConnectionService;
import com.developmentontheedge.be5.database.DataSourceService;
import com.developmentontheedge.be5.database.DbService;
import com.developmentontheedge.be5.api.services.GroovyRegister;
import com.developmentontheedge.be5.api.services.Meta;
import com.developmentontheedge.be5.api.services.ProjectProvider;
import com.developmentontheedge.be5.base.impl.Be5CachesImpl;
import com.developmentontheedge.be5.database.impl.ConnectionServiceImpl;
import com.developmentontheedge.be5.base.impl.DataSourceServiceImpl;
import com.developmentontheedge.be5.database.impl.DbServiceImpl;
import com.developmentontheedge.be5.base.impl.LogConfigurator;
import com.developmentontheedge.be5.base.impl.MetaImpl;
import com.developmentontheedge.be5.base.impl.ProjectProviderImpl;
import com.developmentontheedge.be5.database.impl.SqlHelper;
import com.google.inject.AbstractModule;
import com.google.inject.Scopes;


public class BaseModule extends AbstractModule
{
    @Override
    protected void configure()
    {
        bind(LogConfigurator.class).asEagerSingleton();
        bind(GroovyRegister.class).in(Scopes.SINGLETON);
        bind(SqlHelper.class).in(Scopes.SINGLETON);

        bind(ProjectProvider.class).to(ProjectProviderImpl.class).in(Scopes.SINGLETON);
        bind(DataSourceService.class).to(DataSourceServiceImpl.class).in(Scopes.SINGLETON);
        bind(ConnectionService.class).to(ConnectionServiceImpl.class).in(Scopes.SINGLETON);
        bind(DbService.class).to(DbServiceImpl.class).in(Scopes.SINGLETON);
        bind(Meta.class).to(MetaImpl.class).in(Scopes.SINGLETON);
        bind(UserAwareMeta.class).to(UserAwareMetaImpl.class).in(Scopes.SINGLETON);
        bind(Be5Caches.class).to(Be5CachesImpl.class).in(Scopes.SINGLETON);
    }
}
