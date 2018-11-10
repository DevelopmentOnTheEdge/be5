package com.developmentontheedge.be5.server;

import com.developmentontheedge.be5.base.BaseModule;
import com.developmentontheedge.be5.base.services.UserInfoProvider;
import com.developmentontheedge.be5.database.DatabaseModule;
import com.developmentontheedge.be5.database.impl.SqlHelper;
import com.developmentontheedge.be5.databasemodel.DatabaseModel;
import com.developmentontheedge.be5.databasemodel.helpers.ColumnsHelper;
import com.developmentontheedge.be5.operation.OperationModule;
import com.developmentontheedge.be5.query.QueryModule;
import com.developmentontheedge.be5.server.controllers.DocumentController;
import com.developmentontheedge.be5.server.controllers.DownloadController;
import com.developmentontheedge.be5.server.controllers.FormController;
import com.developmentontheedge.be5.server.controllers.LanguageSelectorController;
import com.developmentontheedge.be5.server.controllers.MenuController;
import com.developmentontheedge.be5.server.controllers.QueryBuilderController;
import com.developmentontheedge.be5.server.controllers.StaticPageController;
import com.developmentontheedge.be5.server.helpers.DpsHelper;
import com.developmentontheedge.be5.server.helpers.ErrorModelHelper;
import com.developmentontheedge.be5.server.helpers.FilterHelper;
import com.developmentontheedge.be5.server.helpers.MenuHelper;
import com.developmentontheedge.be5.server.helpers.UserHelper;
import com.developmentontheedge.be5.server.services.DocumentGenerator;
import com.developmentontheedge.be5.server.services.FormGenerator;
import com.developmentontheedge.be5.server.services.HtmlMetaTags;
import com.developmentontheedge.be5.server.services.events.EventManager;
import com.developmentontheedge.be5.server.services.events.LogBe5Event;
import com.developmentontheedge.be5.server.services.impl.DocumentGeneratorImpl;
import com.developmentontheedge.be5.server.services.impl.FormGeneratorImpl;
import com.developmentontheedge.be5.server.services.impl.HtmlMetaTagsImpl;
import com.developmentontheedge.be5.server.services.impl.UserInfoProviderImpl;
import com.google.inject.Scopes;
import com.google.inject.servlet.ServletModule;

import static com.google.inject.matcher.Matchers.annotatedWith;
import static com.google.inject.matcher.Matchers.any;


public class ServerModule extends ServletModule
{
    @Override
    protected void configureServlets()
    {
        install(new BaseModule());
        install(new DatabaseModule());
        install(new OperationModule());
        install(new QueryModule());

        serve("/api/table*").with(DocumentController.class);
        serve("/api/form*").with(FormController.class);
        serve("/api/static*").with(StaticPageController.class);
        serve("/api/menu*").with(MenuController.class);
        serve("/api/languageSelector*").with(LanguageSelectorController.class);
        serve("/api/queryBuilder").with(QueryBuilderController.class);
        serve("/api/download").with(DownloadController.class);

        bind(FilterHelper.class).in(Scopes.SINGLETON);
        bind(DatabaseModel.class).in(Scopes.SINGLETON);
        bind(DpsHelper.class).in(Scopes.SINGLETON);
        bind(UserHelper.class).in(Scopes.SINGLETON);
        bind(SqlHelper.class).in(Scopes.SINGLETON);
        bind(ColumnsHelper.class).in(Scopes.SINGLETON);
        bind(MenuHelper.class).in(Scopes.SINGLETON);
        bind(ErrorModelHelper.class).in(Scopes.SINGLETON);

        bind(DocumentGenerator.class).to(DocumentGeneratorImpl.class).in(Scopes.SINGLETON);
        bind(FormGenerator.class).to(FormGeneratorImpl.class).in(Scopes.SINGLETON);
        bind(UserInfoProvider.class).to(UserInfoProviderImpl.class).in(Scopes.SINGLETON);
        bind(HtmlMetaTags.class).to(HtmlMetaTagsImpl.class).in(Scopes.SINGLETON);

        EventManager eventManager = new EventManager();
        bind(EventManager.class).toInstance(eventManager);
        requestInjection(eventManager);
        bindInterceptor(any(), annotatedWith(LogBe5Event.class), eventManager);
    }
}
