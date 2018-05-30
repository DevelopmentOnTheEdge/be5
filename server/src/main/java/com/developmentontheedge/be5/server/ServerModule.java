package com.developmentontheedge.be5.server;

import com.developmentontheedge.be5.base.BaseModule;
import com.developmentontheedge.be5.base.UserInfoProvider;
import com.developmentontheedge.be5.database.ConnectionService;
import com.developmentontheedge.be5.database.DbService;
import com.developmentontheedge.be5.database.impl.ConnectionServiceImpl;
import com.developmentontheedge.be5.database.impl.DbServiceImpl;
import com.developmentontheedge.be5.database.impl.SqlHelper;
import com.developmentontheedge.be5.databasemodel.DatabaseModel;
import com.developmentontheedge.be5.databasemodel.helpers.ColumnsHelper;
import com.developmentontheedge.be5.operation.OperationModule;
import com.developmentontheedge.be5.query.services.QueryService;
import com.developmentontheedge.be5.query.services.TableModelService;
import com.developmentontheedge.be5.server.controllers.ApplicationInfoController;
import com.developmentontheedge.be5.server.controllers.DownloadController;
import com.developmentontheedge.be5.server.controllers.FormController;
import com.developmentontheedge.be5.server.controllers.LanguageSelectorController;
import com.developmentontheedge.be5.server.controllers.MenuController;
import com.developmentontheedge.be5.server.controllers.QueryBuilderController;
import com.developmentontheedge.be5.server.controllers.StaticPageController;
import com.developmentontheedge.be5.server.controllers.TableController;
import com.developmentontheedge.be5.server.helpers.DpsHelper;
import com.developmentontheedge.be5.server.helpers.FilterHelper;
import com.developmentontheedge.be5.server.helpers.MenuHelper;
import com.developmentontheedge.be5.server.helpers.OperationHelper;
import com.developmentontheedge.be5.server.helpers.ResponseHelper;
import com.developmentontheedge.be5.server.helpers.UserHelper;
import com.developmentontheedge.be5.server.services.DocumentGenerator;
import com.developmentontheedge.be5.server.services.OperationService;
import com.developmentontheedge.be5.server.services.impl.DocumentGeneratorImpl;
import com.developmentontheedge.be5.server.services.impl.OperationServiceImpl;
import com.developmentontheedge.be5.server.services.impl.QueryServiceImpl;
import com.developmentontheedge.be5.server.services.impl.TableModelServiceImpl;
import com.developmentontheedge.be5.server.services.impl.UserInfoProviderImpl;
import com.google.inject.Scopes;
import com.google.inject.servlet.ServletModule;


public class ServerModule extends ServletModule
{
    @Override
    protected void configureServlets()
    {
        install(new BaseModule());
        install(new OperationModule());

        bind(TableController.class).in(Scopes.SINGLETON);
        bind(FormController.class).in(Scopes.SINGLETON);
        bind(StaticPageController.class).in(Scopes.SINGLETON);
        bind(MenuController.class).in(Scopes.SINGLETON);
        bind(LanguageSelectorController.class).in(Scopes.SINGLETON);
        bind(ApplicationInfoController.class).in(Scopes.SINGLETON);
        bind(QueryBuilderController.class).in(Scopes.SINGLETON);
        bind(DownloadController.class).in(Scopes.SINGLETON);

        serve("/api/table*").with(TableController.class);
        serve("/api/form*").with(FormController.class);
        serve("/api/static*").with(StaticPageController.class);
        serve("/api/menu*").with(MenuController.class);
        serve("/api/languageSelector*").with(LanguageSelectorController.class);
        serve("/api/appInfo").with(ApplicationInfoController.class);
        serve("/api/queryBuilder").with(QueryBuilderController.class);
        serve("/api/download").with(DownloadController.class);

        bind(FilterHelper.class).in(Scopes.SINGLETON);
        bind(DatabaseModel.class).in(Scopes.SINGLETON);
        bind(DpsHelper.class).in(Scopes.SINGLETON);
        bind(UserHelper.class).in(Scopes.SINGLETON);
        bind(OperationHelper.class).in(Scopes.SINGLETON);
        bind(SqlHelper.class).in(Scopes.SINGLETON);
        bind(ColumnsHelper.class).in(Scopes.SINGLETON);
        bind(MenuHelper.class).in(Scopes.SINGLETON);
        bind(ResponseHelper.class).in(Scopes.SINGLETON);

        bind(ConnectionService.class).to(ConnectionServiceImpl.class).in(Scopes.SINGLETON);
        bind(DbService.class).to(DbServiceImpl.class).in(Scopes.SINGLETON);
        bind(QueryService.class).to(QueryServiceImpl.class).in(Scopes.SINGLETON);
        bind(OperationService.class).to(OperationServiceImpl.class).in(Scopes.SINGLETON);
        bind(TableModelService.class).to(TableModelServiceImpl.class).in(Scopes.SINGLETON);
        bind(DocumentGenerator.class).to(DocumentGeneratorImpl.class).in(Scopes.SINGLETON);
        bind(UserInfoProvider.class).to(UserInfoProviderImpl.class).in(Scopes.SINGLETON);
    }
}
