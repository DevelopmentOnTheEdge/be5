package com.developmentontheedge.be5.test;

import ch.qos.logback.classic.Level;
import com.developmentontheedge.be5.Bootstrap;
import com.developmentontheedge.be5.security.UserInfo;
import com.developmentontheedge.be5.security.UserInfoHolder;
import com.developmentontheedge.be5.cache.Be5Caches;
import com.developmentontheedge.be5.meta.Meta;
import com.developmentontheedge.be5.meta.ProjectProvider;
import com.developmentontheedge.be5.meta.UserAwareMeta;
import com.developmentontheedge.be5.security.UserInfoProvider;
import com.developmentontheedge.be5.logging.LogConfigurator;
import com.developmentontheedge.be5.database.ConnectionService;
import com.developmentontheedge.be5.database.DataSourceService;
import com.developmentontheedge.be5.database.DbService;
import com.developmentontheedge.be5.metadata.exception.ProjectLoadException;
import com.developmentontheedge.be5.metadata.model.BeConnectionProfile;
import com.developmentontheedge.be5.metadata.model.DataElementUtils;
import com.developmentontheedge.be5.metadata.model.Project;
import com.developmentontheedge.be5.metadata.scripts.AppDb;
import com.developmentontheedge.be5.metadata.scripts.AppDropAllTables;
import com.developmentontheedge.be5.metadata.serialization.ModuleLoader2;
import com.developmentontheedge.be5.metadata.util.JULLogger;
import com.developmentontheedge.be5.metadata.util.ProjectTestUtils;
import com.developmentontheedge.be5.test.mocks.Be5CachesForTest;
import com.developmentontheedge.be5.test.mocks.ConnectionServiceMock;
import com.developmentontheedge.be5.test.mocks.DataSourceServiceMock;
import com.developmentontheedge.be5.test.mocks.DbServiceMock;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Scopes;
import com.google.inject.Stage;
import org.junit.Before;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.logging.Logger;


public abstract class BaseTest extends BaseTestUtils implements InjectedTestUtils
{
    static
    {
        LogConfigurator.configure();
        LogConfigurator.setLevel(Level.INFO);
    }

    public static final Logger log = Logger.getLogger(BaseTest.class.getName());
    private static final String profileForIntegrationTests = "profileForIntegrationTests";
    private static final String ITest_ = "ITest_";

    @Inject
    protected Meta meta;
    @Inject
    protected UserAwareMeta userAwareMeta;
    @Inject
    protected DbService db;

    @Inject
    protected UserInfoProvider userInfoProvider;

    protected static final String TEST_USER = "testUser";

    @Before
    public void setUpBaseTestUtils()
    {
        getInjector().injectMembers(this);
    }

    protected void setStaticUserInfo(String... roles)
    {
        UserInfo userInfo = new UserInfo(TEST_USER, Arrays.asList(roles), Arrays.asList(roles));
        userInfo.setRemoteAddr("192.168.0.1");
        UserInfoHolder.setLoggedUser(userInfo);
    }

    protected static Injector initInjector(Module... modules)
    {
        Injector injector = Guice.createInjector(Stage.PRODUCTION, modules);
        new Bootstrap(injector).boot();
        return injector;
    }

    protected static void initDb()
    {
        Project project = findProject();
        initDb(project);
    }

    protected static void addH2ProfileAndCreateDb()
    {
        Project project = findProject();
        addH2Profile(project);
        initDb(project);
    }

    protected static Project findProject()
    {
        Project project;
        try
        {
            project = ModuleLoader2.findAndLoadProjectWithModules(false, new JULLogger(log));
        }
        catch (ProjectLoadException e)
        {
            throw new RuntimeException(e);
        }
        return project;
    }

    protected static void addH2Profile()
    {
        Project project = findProject();
        addH2Profile(project);
    }

    protected static void initDb(Project project)
    {
        if (isITestProfile(project.getConnectionProfile()))
        {
            log.info("Execute AppDropAllTables");
            new AppDropAllTables()
                    .setBe5Project(project)
                    .execute();

            log.info("Execute AppDb");
            new AppDb()
                    .setBe5Project(project)
                    .execute();
        }
        else
        {
            log.warning("For integration tests allowed only '" + profileForIntegrationTests
                    + "' profile name or start with '" + ITest_ + "'.");
        }
    }

    public static boolean isITestProfile(BeConnectionProfile connectionProfile)
    {
        return connectionProfile != null && (connectionProfile.getName().equals(profileForIntegrationTests) ||
                connectionProfile.getName().startsWith(ITest_));
    }

    public static BeConnectionProfile addProfile(Project project, String url, String userName, String password)
    {
        BeConnectionProfile profile = new BeConnectionProfile(profileForIntegrationTests,
                project.getConnectionProfiles().getLocalProfiles());
        profile.setConnectionUrl(url);
        profile.setUsername(userName);
        profile.setPassword(password);
        DataElementUtils.save(profile);
        project.setConnectionProfileName(profileForIntegrationTests);
        return project.getConnectionProfile();
    }

    protected static BeConnectionProfile addH2Profile(Project project)
    {
        if (!isITestProfile(project.getConnectionProfile()))
        {
            ProjectTestUtils.createH2Profile(project, ITest_ + project.getName());
            project.setConnectionProfileName(ITest_ + project.getName());
        }
        return project.getConnectionProfile();
    }

    public static class BaseDbMockTestModule extends AbstractModule
    {
        @Override
        protected void configure()
        {
            bind(ProjectProvider.class).to(TestProjectProvider.class).in(Scopes.SINGLETON);

            bind(DbService.class).to(DbServiceMock.class).in(Scopes.SINGLETON);
            bind(DataSourceService.class).to(DataSourceServiceMock.class).in(Scopes.SINGLETON);
            bind(ConnectionService.class).to(ConnectionServiceMock.class).in(Scopes.SINGLETON);
            bind(Be5Caches.class).to(Be5CachesForTest.class).in(Scopes.SINGLETON);
        }
    }

    public static class BaseDbTestModule extends AbstractModule
    {
        @Override
        protected void configure()
        {
            bind(ProjectProvider.class).to(TestProjectProvider.class).in(Scopes.SINGLETON);
            bind(Be5Caches.class).to(Be5CachesForTest.class).in(Scopes.SINGLETON);
        }
    }
}
