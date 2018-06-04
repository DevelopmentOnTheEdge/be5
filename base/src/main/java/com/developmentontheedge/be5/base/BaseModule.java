package com.developmentontheedge.be5.base;

import com.developmentontheedge.be5.base.model.groovy.DynamicPropertyMetaClass;
import com.developmentontheedge.be5.base.model.groovy.DynamicPropertySetMetaClass;
import com.developmentontheedge.be5.base.services.Be5Caches;
import com.developmentontheedge.be5.base.services.GroovyRegister;
import com.developmentontheedge.be5.base.services.Meta;
import com.developmentontheedge.be5.base.services.ProjectProvider;
import com.developmentontheedge.be5.base.services.UserAwareMeta;
import com.developmentontheedge.be5.base.services.impl.Be5CachesImpl;
import com.developmentontheedge.be5.base.services.impl.DataSourceServiceImpl;
import com.developmentontheedge.be5.base.services.impl.LogConfigurator;
import com.developmentontheedge.be5.base.services.impl.MetaImpl;
import com.developmentontheedge.be5.base.services.impl.ProjectProviderImpl;
import com.developmentontheedge.be5.base.services.impl.UserAwareMetaImpl;
import com.developmentontheedge.be5.database.ConnectionService;
import com.developmentontheedge.be5.database.DataSourceService;
import com.developmentontheedge.be5.database.DbService;
import com.developmentontheedge.be5.database.impl.ConnectionServiceImpl;
import com.developmentontheedge.be5.database.impl.DbServiceImpl;
import com.developmentontheedge.be5.database.impl.SqlHelper;
import com.developmentontheedge.beans.DynamicProperty;
import com.developmentontheedge.beans.DynamicPropertySetDecorator;
import com.developmentontheedge.beans.DynamicPropertySetSupport;
import com.google.inject.AbstractModule;
import com.google.inject.Scopes;


public class BaseModule extends AbstractModule
{
    static
    {
        GroovyRegister.registerMetaClass( DynamicPropertyMetaClass.class, DynamicProperty.class );
        GroovyRegister.registerMetaClass( DynamicPropertySetMetaClass.class, DynamicPropertySetSupport.class );
        GroovyRegister.registerMetaClass( DynamicPropertySetMetaClass.class, DynamicPropertySetDecorator.class );
    }

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
