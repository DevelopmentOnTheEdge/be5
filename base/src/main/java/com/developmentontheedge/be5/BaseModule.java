package com.developmentontheedge.be5;

import com.developmentontheedge.be5.cache.Be5Caches;
import com.developmentontheedge.be5.cache.Be5CachesImpl;
import com.developmentontheedge.be5.groovy.GroovyRegister;
import com.developmentontheedge.be5.lifecycle.LifecycleSupport;
import com.developmentontheedge.be5.mail.MailService;
import com.developmentontheedge.be5.mail.MailServiceImpl;
import com.developmentontheedge.be5.meta.Meta;
import com.developmentontheedge.be5.meta.MetaImpl;
import com.developmentontheedge.be5.meta.ProjectProvider;
import com.developmentontheedge.be5.meta.ProjectProviderImpl;
import com.developmentontheedge.be5.meta.UserAwareMeta;
import com.developmentontheedge.be5.meta.UserAwareMetaImpl;
import com.developmentontheedge.be5.security.UserInfo;
import com.developmentontheedge.be5.model.groovy.DynamicPropertyMetaClass;
import com.developmentontheedge.be5.model.groovy.DynamicPropertySetMetaClass;
import com.developmentontheedge.be5.scheduling.DaemonStarter;
import com.developmentontheedge.be5.scheduling.DaemonStarterImpl;
import com.developmentontheedge.be5.scheduling.GuiceJobFactory;
import com.developmentontheedge.be5.security.UserInfoHolder;
import com.developmentontheedge.be5.security.UserInfoProvider;
import com.developmentontheedge.be5.security.UserInfoProviderImpl;
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
        bind(MailService.class).to(MailServiceImpl.class).in(Scopes.SINGLETON);
    }

    @Provides
    UserInfo provideUserInfo()
    {
        return UserInfoHolder.getLoggedUser();
    }
}
