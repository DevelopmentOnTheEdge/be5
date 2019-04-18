package com.developmentontheedge.be5.server;

import com.developmentontheedge.be5.server.controllers.DocumentController;
import com.developmentontheedge.be5.server.controllers.DownloadController;
import com.developmentontheedge.be5.server.controllers.DownloadOperationController;
import com.developmentontheedge.be5.server.controllers.FormController;
import com.developmentontheedge.be5.server.controllers.LanguageSelectorController;
import com.developmentontheedge.be5.server.controllers.MenuController;
import com.developmentontheedge.be5.server.controllers.QueryBuilderController;
import com.developmentontheedge.be5.server.controllers.ReloadProjectController;
import com.developmentontheedge.be5.server.controllers.StaticPageController;
import com.developmentontheedge.be5.server.controllers.UserInfoController;
import com.google.inject.servlet.ServletModule;


public class ServerServletModule extends ServletModule
{
    @Override
    protected void configureServlets()
    {
        serve("/api/table*").with(DocumentController.class);
        serve("/api/form*").with(FormController.class);
        serve("/api/static*").with(StaticPageController.class);
        serve("/api/menu*").with(MenuController.class);
        serve("/api/userInfo*").with(UserInfoController.class);
        serve("/api/languageSelector*").with(LanguageSelectorController.class);
        serve("/api/queryBuilder*").with(QueryBuilderController.class);
        serve("/api/download").with(DownloadController.class);
        serve("/api/downloadOperation").with(DownloadOperationController.class);
        serve("/api/reloadProject").with(ReloadProjectController.class);
    }
}
