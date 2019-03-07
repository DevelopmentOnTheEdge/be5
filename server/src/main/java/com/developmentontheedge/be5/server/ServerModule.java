package com.developmentontheedge.be5.server;

import com.developmentontheedge.be5.BaseModule;
import com.developmentontheedge.be5.database.DatabaseModule;
import com.developmentontheedge.be5.databasemodel.DatabaseModel;
import com.developmentontheedge.be5.databasemodel.helpers.ColumnsHelper;
import com.developmentontheedge.be5.databasemodel.helpers.SqlHelper;
import com.developmentontheedge.be5.operation.OperationModule;
import com.developmentontheedge.be5.query.QueryModule;
import com.developmentontheedge.be5.query.QuerySession;
import com.developmentontheedge.be5.server.services.DocumentFormPlugin;
import com.developmentontheedge.be5.server.services.DocumentGenerator;
import com.developmentontheedge.be5.server.services.DocumentOperationsPlugin;
import com.developmentontheedge.be5.server.services.DpsHelper;
import com.developmentontheedge.be5.server.services.ErrorModelHelper;
import com.developmentontheedge.be5.server.services.FilterHelper;
import com.developmentontheedge.be5.server.services.FilterInfoPlugin;
import com.developmentontheedge.be5.server.services.FormGenerator;
import com.developmentontheedge.be5.server.services.HtmlMetaTags;
import com.developmentontheedge.be5.server.services.InitUserService;
import com.developmentontheedge.be5.server.services.MenuHelper;
import com.developmentontheedge.be5.server.services.UserInfoModelService;
import com.developmentontheedge.be5.server.services.events.EventManager;
import com.developmentontheedge.be5.server.services.events.LogBe5Event;
import com.developmentontheedge.be5.server.services.impl.DocumentGeneratorImpl;
import com.developmentontheedge.be5.server.services.impl.FormGeneratorImpl;
import com.developmentontheedge.be5.server.services.impl.HtmlMetaTagsImpl;
import com.developmentontheedge.be5.server.services.impl.InitUserServiceImpl;
import com.developmentontheedge.be5.server.services.impl.QuerySessionImpl;
import com.developmentontheedge.be5.server.services.impl.UserInfoModelServiceImpl;
import com.developmentontheedge.be5.server.services.impl.rows.TableRowBuilder;
import com.developmentontheedge.be5.server.services.rememberme.PersistentTokenRepository;
import com.developmentontheedge.be5.server.services.rememberme.PersistentTokenRepositoryImpl;
import com.developmentontheedge.be5.server.services.rememberme.RememberMeServices;
import com.developmentontheedge.be5.server.services.rememberme.RememberMeServicesImpl;
import com.developmentontheedge.be5.server.services.users.UserService;
import com.developmentontheedge.be5.web.Session;
import com.developmentontheedge.be5.web.impl.SessionImpl;
import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

import static com.google.inject.matcher.Matchers.annotatedWith;
import static com.google.inject.matcher.Matchers.any;


public class ServerModule extends AbstractModule
{
    @Override
    protected void configure()
    {
        install(new BaseModule());
        bind(Session.class).to(SessionImpl.class).in(Scopes.SINGLETON);
        bind(QuerySession.class).to(QuerySessionImpl.class).in(Scopes.SINGLETON);

        EventManager eventManager = new EventManager();
        bind(EventManager.class).toInstance(eventManager);
        requestInjection(eventManager);
        bindInterceptor(any(), annotatedWith(LogBe5Event.class), eventManager);

        install(new DatabaseModule());
        install(new OperationModule());
        install(new QueryModule());

        bind(FilterHelper.class).in(Scopes.SINGLETON);
        bind(DatabaseModel.class).in(Scopes.SINGLETON);
        bind(DpsHelper.class).in(Scopes.SINGLETON);
        bind(UserService.class).in(Scopes.SINGLETON);
        bind(SqlHelper.class).in(Scopes.SINGLETON);
        bind(ColumnsHelper.class).in(Scopes.SINGLETON);
        bind(MenuHelper.class).in(Scopes.SINGLETON);
        bind(ErrorModelHelper.class).in(Scopes.SINGLETON);
        bind(TableRowBuilder.class).in(Scopes.SINGLETON);

        bind(DocumentOperationsPlugin.class).asEagerSingleton();
        bind(DocumentFormPlugin.class).asEagerSingleton();
        bind(FilterInfoPlugin.class).asEagerSingleton();

        bind(DocumentGenerator.class).to(DocumentGeneratorImpl.class).in(Scopes.SINGLETON);
        bind(FormGenerator.class).to(FormGeneratorImpl.class).in(Scopes.SINGLETON);
        bind(HtmlMetaTags.class).to(HtmlMetaTagsImpl.class).in(Scopes.SINGLETON);
        bind(UserInfoModelService.class).to(UserInfoModelServiceImpl.class).in(Scopes.SINGLETON);
        bind(InitUserService.class).to(InitUserServiceImpl.class).in(Scopes.SINGLETON);
        bind(RememberMeServices.class).to(RememberMeServicesImpl.class).in(Scopes.SINGLETON);
        bind(PersistentTokenRepository.class).to(PersistentTokenRepositoryImpl.class).in(Scopes.SINGLETON);
    }
}
