package com.developmentontheedge.be5.modules.core;

import com.developmentontheedge.be5.modules.core.controllers.CategoriesController;
import com.developmentontheedge.be5.modules.core.controllers.SaveQuickColumnSetting;
import com.developmentontheedge.be5.server.ServerServletModule;
import com.google.inject.servlet.ServletModule;


public class CoreServletModule extends ServletModule
{
    @Override
    protected void configureServlets()
    {
        install(new ServerServletModule());
        serve("/api/categories*").with(CategoriesController.class);
        serve("/api/quick*").with(SaveQuickColumnSetting.class);
    }
}
