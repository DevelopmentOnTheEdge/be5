package com.developmentontheedge.be5.modules.core;

import com.developmentontheedge.be5.api.services.CategoriesService;
import com.developmentontheedge.be5.api.services.CoreUtils;
import com.developmentontheedge.be5.modules.core.components.Categories;
import com.developmentontheedge.be5.modules.core.components.UserInfoComponent;
import com.developmentontheedge.be5.modules.core.services.LoginService;
import com.developmentontheedge.be5.modules.core.services.impl.CategoriesServiceImpl;
import com.developmentontheedge.be5.modules.core.services.impl.CoreUtilsImpl;
import com.developmentontheedge.be5.modules.core.services.impl.LoginServiceImpl;
import com.google.inject.Scopes;
import com.google.inject.servlet.ServletModule;


public class CoreModule extends ServletModule
{
    @Override
    protected void configureServlets()
    {
        bind(UserInfoComponent.class).in(Scopes.SINGLETON);
        bind(Categories.class).in(Scopes.SINGLETON);

        serve("/api/userInfo*").with(UserInfoComponent.class);
        serve("/api/categories*").with(Categories.class);

        bind(CoreUtils.class).to(CoreUtilsImpl.class).in(Scopes.SINGLETON);
        bind(LoginService.class).to(LoginServiceImpl.class).in(Scopes.SINGLETON);
        bind(CategoriesService.class).to(CategoriesServiceImpl.class).in(Scopes.SINGLETON);
    }
}
