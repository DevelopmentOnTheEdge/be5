package com.developmentontheedge.be5.modules.core;

import com.developmentontheedge.be5.config.CoreUtils;
import com.developmentontheedge.be5.modules.core.services.CategoriesService;
import com.developmentontheedge.be5.modules.core.services.DocumentCategoriesPlugin;
import com.developmentontheedge.be5.modules.core.services.LoginService;
import com.developmentontheedge.be5.modules.core.services.RoleServiceImpl;
import com.developmentontheedge.be5.modules.core.services.impl.Be5EventDbLogger;
import com.developmentontheedge.be5.modules.core.services.impl.CategoriesHelper;
import com.developmentontheedge.be5.modules.core.services.impl.CategoriesServiceImpl;
import com.developmentontheedge.be5.modules.core.services.impl.CoreUtilsImpl;
import com.developmentontheedge.be5.modules.core.services.impl.LoginServiceImpl;
import com.developmentontheedge.be5.modules.core.services.impl.OperationLoggingImpl;
import com.developmentontheedge.be5.server.ServerModule;
import com.developmentontheedge.be5.server.services.OperationLogging;
import com.developmentontheedge.be5.server.services.users.RoleService;
import com.google.inject.AbstractModule;
import com.google.inject.Scopes;


public class CoreModule extends AbstractModule
{
    @Override
    protected void configure()
    {
        install(new ServerModule());

        bind(CategoriesHelper.class).in(Scopes.SINGLETON);
        bind(RoleService.class).to(RoleServiceImpl.class).in(Scopes.SINGLETON);
        bind(CoreUtils.class).to(CoreUtilsImpl.class).in(Scopes.SINGLETON);
        bind(LoginService.class).to(LoginServiceImpl.class).in(Scopes.SINGLETON);
        bind(CategoriesService.class).to(CategoriesServiceImpl.class).in(Scopes.SINGLETON);
        bind(DocumentCategoriesPlugin.class).asEagerSingleton();
        bind(Be5EventDbLogger.class).asEagerSingleton();

        bind(OperationLogging.class).to(OperationLoggingImpl.class).in(Scopes.SINGLETON);
    }
}
