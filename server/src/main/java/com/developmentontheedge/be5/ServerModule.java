package com.developmentontheedge.be5;

import com.developmentontheedge.be5.api.helpers.ResponseHelper;
import com.developmentontheedge.be5.api.services.OperationsFactory;
import com.developmentontheedge.be5.api.services.impl.OperationsFactoryImpl;
import com.developmentontheedge.be5.databasemodel.helpers.ColumnsHelper;
import com.developmentontheedge.be5.api.helpers.DpsHelper;
import com.developmentontheedge.be5.api.helpers.FilterHelper;
import com.developmentontheedge.be5.api.helpers.MenuHelper;
import com.developmentontheedge.be5.api.helpers.OperationHelper;
import com.developmentontheedge.be5.base.services.UserAwareMeta;
import com.developmentontheedge.be5.api.helpers.UserHelper;
import com.developmentontheedge.be5.base.UserInfoProvider;
import com.developmentontheedge.be5.base.services.impl.UserAwareMetaImpl;
import com.developmentontheedge.be5.api.services.impl.UserInfoProviderImpl;
import com.developmentontheedge.be5.base.services.Be5Caches;
import com.developmentontheedge.be5.database.ConnectionService;
import com.developmentontheedge.be5.database.DataSourceService;
import com.developmentontheedge.be5.api.services.DocumentGenerator;
import com.developmentontheedge.be5.base.services.GroovyRegister;
import com.developmentontheedge.be5.base.services.Meta;
import com.developmentontheedge.be5.api.services.OperationExecutor;
import com.developmentontheedge.be5.api.services.OperationService;
import com.developmentontheedge.be5.base.services.ProjectProvider;
import com.developmentontheedge.be5.api.services.QueryService;
import com.developmentontheedge.be5.database.DbService;
import com.developmentontheedge.be5.api.services.TableModelService;
import com.developmentontheedge.be5.databasemodel.DatabaseModel;
import com.developmentontheedge.be5.base.impl.Be5CachesImpl;
import com.developmentontheedge.be5.database.impl.ConnectionServiceImpl;
import com.developmentontheedge.be5.base.impl.DataSourceServiceImpl;
import com.developmentontheedge.be5.api.services.impl.DocumentGeneratorImpl;
import com.developmentontheedge.be5.api.services.GroovyOperationLoader;
import com.developmentontheedge.be5.base.impl.LogConfigurator;
import com.developmentontheedge.be5.base.impl.MetaImpl;
import com.developmentontheedge.be5.api.services.impl.OperationExecutorImpl;
import com.developmentontheedge.be5.api.services.impl.OperationServiceImpl;
import com.developmentontheedge.be5.base.impl.ProjectProviderImpl;
import com.developmentontheedge.be5.api.services.impl.QueryServiceImpl;
import com.developmentontheedge.be5.database.impl.SqlHelper;
import com.developmentontheedge.be5.database.impl.DbServiceImpl;
import com.developmentontheedge.be5.api.services.impl.TableModelServiceImpl;
import com.developmentontheedge.be5.api.services.validation.Validator;
import com.developmentontheedge.be5.controllers.ApplicationInfoController;
import com.developmentontheedge.be5.controllers.DownloadController;
import com.developmentontheedge.be5.controllers.FormController;
import com.developmentontheedge.be5.controllers.LanguageSelectorController;
import com.developmentontheedge.be5.controllers.MenuController;
import com.developmentontheedge.be5.controllers.QueryBuilderController;
import com.developmentontheedge.be5.controllers.StaticPageController;
import com.developmentontheedge.be5.controllers.TableController;
import com.google.inject.Scopes;
import com.google.inject.servlet.ServletModule;


public class ServerModule extends ServletModule
{
    @Override
    protected void configureServlets()
    {
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

        bind(LogConfigurator.class).asEagerSingleton();
        bind(FilterHelper.class).in(Scopes.SINGLETON);
        bind(DatabaseModel.class).in(Scopes.SINGLETON);
        bind(DpsHelper.class).in(Scopes.SINGLETON);
        bind(UserHelper.class).in(Scopes.SINGLETON);
        bind(Validator.class).in(Scopes.SINGLETON);
        bind(OperationHelper.class).in(Scopes.SINGLETON);
        bind(GroovyOperationLoader.class).in(Scopes.SINGLETON);
        bind(GroovyRegister.class).in(Scopes.SINGLETON);
        bind(SqlHelper.class).in(Scopes.SINGLETON);
        bind(ColumnsHelper.class).in(Scopes.SINGLETON);
        bind(MenuHelper.class).in(Scopes.SINGLETON);
        bind(ResponseHelper.class).in(Scopes.SINGLETON);

        bind(ProjectProvider.class).to(ProjectProviderImpl.class).in(Scopes.SINGLETON);
        bind(DataSourceService.class).to(DataSourceServiceImpl.class).in(Scopes.SINGLETON);
        bind(ConnectionService.class).to(ConnectionServiceImpl.class).in(Scopes.SINGLETON);
        bind(DbService.class).to(DbServiceImpl.class).in(Scopes.SINGLETON);
        bind(Meta.class).to(MetaImpl.class).in(Scopes.SINGLETON);
        bind(UserAwareMeta.class).to(UserAwareMetaImpl.class).in(Scopes.SINGLETON);
        bind(QueryService.class).to(QueryServiceImpl.class).in(Scopes.SINGLETON);
        bind(OperationService.class).to(OperationServiceImpl.class).in(Scopes.SINGLETON);
        bind(OperationExecutor.class).to(OperationExecutorImpl.class).in(Scopes.SINGLETON);
        bind(OperationsFactory.class).to(OperationsFactoryImpl.class).in(Scopes.SINGLETON);
        bind(TableModelService.class).to(TableModelServiceImpl.class).in(Scopes.SINGLETON);
        bind(DocumentGenerator.class).to(DocumentGeneratorImpl.class).in(Scopes.SINGLETON);
        bind(Be5Caches.class).to(Be5CachesImpl.class).in(Scopes.SINGLETON);
        bind(UserInfoProvider.class).to(UserInfoProviderImpl.class).in(Scopes.SINGLETON);
    }
}
