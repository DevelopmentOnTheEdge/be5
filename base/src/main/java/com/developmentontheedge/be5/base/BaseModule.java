package com.developmentontheedge.be5.base;

import com.developmentontheedge.be5.base.lifecycle.LifecycleSupport;
import com.developmentontheedge.be5.base.model.UserInfo;
import com.developmentontheedge.be5.base.model.groovy.DynamicPropertyMetaClass;
import com.developmentontheedge.be5.base.model.groovy.DynamicPropertySetMetaClass;
import com.developmentontheedge.be5.base.scheduling.DaemonStarter;
import com.developmentontheedge.be5.base.scheduling.DaemonStarterImpl;
import com.developmentontheedge.be5.base.scheduling.GuiceJobFactory;
import com.developmentontheedge.be5.base.security.UserInfoHolder;
import com.developmentontheedge.be5.base.security.UserInfoProvider;
import com.developmentontheedge.be5.base.security.UserInfoProviderImpl;
import com.developmentontheedge.be5.base.services.Be5Caches;
import com.developmentontheedge.be5.base.services.GroovyRegister;
import com.developmentontheedge.be5.base.services.Meta;
import com.developmentontheedge.be5.base.services.ProjectProvider;
import com.developmentontheedge.be5.base.services.UserAwareMeta;
import com.developmentontheedge.be5.base.services.impl.Be5CachesImpl;
import com.developmentontheedge.be5.base.services.impl.MetaImpl;
import com.developmentontheedge.be5.base.services.impl.ProjectProviderImpl;
import com.developmentontheedge.be5.base.services.impl.UserAwareMetaImpl;
import com.developmentontheedge.be5.metadata.model.Project;
import com.developmentontheedge.beans.DynamicProperty;
import com.developmentontheedge.beans.DynamicPropertySetDecorator;
import com.developmentontheedge.beans.DynamicPropertySetSupport;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Scopes;


public class BaseModule extends AbstractModule
{
    static
    {
        GroovyRegister.registerMetaClass(DynamicPropertyMetaClass.class, DynamicProperty.class);
        GroovyRegister.registerMetaClass(DynamicPropertySetMetaClass.class, DynamicPropertySetSupport.class);
        GroovyRegister.registerMetaClass(DynamicPropertySetMetaClass.class, DynamicPropertySetDecorator.class);
    }

    @Override
    protected void configure()
    {
        install(LifecycleSupport.getModule());
        bind(ProjectProvider.class).to(ProjectProviderImpl.class).in(Scopes.SINGLETON);
        bind(Meta.class).to(MetaImpl.class).in(Scopes.SINGLETON);
        bind(UserAwareMeta.class).to(UserAwareMetaImpl.class).in(Scopes.SINGLETON);
        bind(GroovyRegister.class).in(Scopes.SINGLETON);
        bind(Be5Caches.class).to(Be5CachesImpl.class).in(Scopes.SINGLETON);
        bind(UserInfoProvider.class).to(UserInfoProviderImpl.class).in(Scopes.SINGLETON);
        bind(GuiceJobFactory.class).in(Scopes.SINGLETON);
        bind(DaemonStarter.class).to(DaemonStarterImpl.class).asEagerSingleton();
        bind(Project.class).toProvider(ProjectProvider.class);
    }

    @Provides
    UserInfo provideUserInfo()
    {
        return UserInfoHolder.getLoggedUser();
    }
}
