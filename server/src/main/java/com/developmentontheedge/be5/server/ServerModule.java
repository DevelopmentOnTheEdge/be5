package com.developmentontheedge.be5.server;

import com.developmentontheedge.be5.BaseModule;
import com.developmentontheedge.be5.database.DatabaseModule;
import com.developmentontheedge.be5.databasemodel.DatabaseModel;
import com.developmentontheedge.be5.databasemodel.helpers.ColumnsHelper;
import com.developmentontheedge.be5.databasemodel.helpers.SqlHelper;
import com.developmentontheedge.be5.operation.OperationModule;
import com.developmentontheedge.be5.query.QueryModule;
import com.developmentontheedge.be5.query.QuerySession;
import com.developmentontheedge.be5.query.impl.beautifiers.SubQueryBeautifier;
import com.developmentontheedge.be5.server.authentication.InitUserService;
import com.developmentontheedge.be5.server.authentication.InitUserServiceImpl;
import com.developmentontheedge.be5.server.authentication.UserInfoModelService;
import com.developmentontheedge.be5.server.authentication.UserInfoModelServiceImpl;
import com.developmentontheedge.be5.server.authentication.UserService;
import com.developmentontheedge.be5.server.authentication.rememberme.PersistentTokenRepository;
import com.developmentontheedge.be5.server.authentication.rememberme.PersistentTokenRepositoryImpl;
import com.developmentontheedge.be5.server.authentication.rememberme.RememberMeServices;
import com.developmentontheedge.be5.server.authentication.rememberme.ThrottlingRememberMeService;
import com.developmentontheedge.be5.server.queries.JsonBeautifier;
import com.developmentontheedge.be5.server.services.DpsHelper;
import com.developmentontheedge.be5.server.services.ErrorModelHelper;
import com.developmentontheedge.be5.server.services.FormGenerator;
import com.developmentontheedge.be5.server.services.HtmlMetaTags;
import com.developmentontheedge.be5.server.services.MenuHelper;
import com.developmentontheedge.be5.server.services.document.DocumentFormPlugin;
import com.developmentontheedge.be5.server.services.document.DocumentGenerator;
import com.developmentontheedge.be5.server.services.document.DocumentGeneratorImpl;
import com.developmentontheedge.be5.server.services.document.DocumentOperationsPlugin;
import com.developmentontheedge.be5.server.services.document.FilterInfoPlugin;
import com.developmentontheedge.be5.server.services.document.rows.TableRowBuilder;
import com.developmentontheedge.be5.server.services.events.EventManager;
import com.developmentontheedge.be5.server.services.events.LogBe5Event;
import com.developmentontheedge.be5.server.services.impl.FormGeneratorImpl;
import com.developmentontheedge.be5.server.services.impl.HtmlMetaTagsImpl;
import com.developmentontheedge.be5.server.services.impl.QuerySessionImpl;
import com.developmentontheedge.be5.web.Session;
import com.developmentontheedge.be5.web.impl.SessionImpl;
import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.multibindings.MapBinder;

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
        bind(RememberMeServices.class).to(ThrottlingRememberMeService.class).in(Scopes.SINGLETON);
        bind(PersistentTokenRepository.class).to(PersistentTokenRepositoryImpl.class).in(Scopes.SINGLETON);

        MapBinder<String, SubQueryBeautifier> binder = MapBinder.newMapBinder(binder(),
                String.class, SubQueryBeautifier.class);
        binder.addBinding("json").to(JsonBeautifier.class);
    }
}
