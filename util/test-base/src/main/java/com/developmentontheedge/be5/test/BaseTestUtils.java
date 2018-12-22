package com.developmentontheedge.be5.test;

import ch.qos.logback.classic.Level;
import com.developmentontheedge.be5.base.model.UserInfo;
import com.developmentontheedge.be5.base.services.Be5Caches;
import com.developmentontheedge.be5.base.services.Meta;
import com.developmentontheedge.be5.base.services.ProjectProvider;
import com.developmentontheedge.be5.base.services.UserAwareMeta;
import com.developmentontheedge.be5.base.services.UserInfoProvider;
import com.developmentontheedge.be5.base.services.impl.LogConfigurator;
import com.developmentontheedge.be5.database.ConnectionService;
import com.developmentontheedge.be5.database.DataSourceService;
import com.developmentontheedge.be5.database.DbService;
import com.developmentontheedge.be5.database.sql.ResultSetParser;
import com.developmentontheedge.be5.database.sql.parsers.ConcatColumnsParser;
import com.developmentontheedge.be5.metadata.exception.ProjectLoadException;
import com.developmentontheedge.be5.metadata.model.BeConnectionProfile;
import com.developmentontheedge.be5.metadata.model.DataElementUtils;
import com.developmentontheedge.be5.metadata.model.Project;
import com.developmentontheedge.be5.metadata.scripts.AppDb;
import com.developmentontheedge.be5.metadata.scripts.AppDropAllTables;
import com.developmentontheedge.be5.metadata.serialization.ModuleLoader2;
import com.developmentontheedge.be5.metadata.util.ProjectTestUtils;
import com.developmentontheedge.be5.test.mocks.Be5CachesForTest;
import com.developmentontheedge.be5.test.mocks.ConnectionServiceMock;
import com.developmentontheedge.be5.test.mocks.DataSourceServiceMock;
import com.developmentontheedge.be5.test.mocks.DbServiceMock;
import com.developmentontheedge.be5.testbase.StaticUserInfoProvider;
import com.developmentontheedge.beans.DynamicProperty;
import com.developmentontheedge.beans.DynamicPropertySet;
import com.developmentontheedge.beans.DynamicPropertySetSupport;
import com.google.common.collect.ImmutableMap;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Scopes;
import com.google.inject.Stage;
import org.junit.Before;
import org.mockito.Matchers;

import javax.inject.Inject;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static org.mockito.Matchers.anyVararg;
import static org.mockito.Matchers.contains;
import static org.mockito.Mockito.when;


public abstract class BaseTestUtils
{
    static
    {
        LogConfigurator.configure();
        LogConfigurator.setLevel(Level.INFO);
    }

    public static final Logger log = Logger.getLogger(BaseTestUtils.class.getName());
    private static final String profileForIntegrationTests = "profileForIntegrationTests";
    private static final String ITest_ = "ITest_";

    protected static final Jsonb jsonb = JsonbBuilder.create();

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
        if (getInjector() != null)
        {
            getInjector().injectMembers(this);
        }
    }

    public Injector getInjector()
    {
        return null;
    }

    protected void setStaticUserInfo(String... roles)
    {
        StaticUserInfoProvider.userInfo = new UserInfo(TEST_USER, Arrays.asList(roles), Arrays.asList(roles));
        StaticUserInfoProvider.userInfo.setRemoteAddr("192.168.0.1");
    }

    protected static Injector initInjector(Module... modules)
    {
        return Guice.createInjector(Stage.PRODUCTION, modules);
    }

    protected static String oneQuotes(Object s)
    {
        return s.toString().replace("\"", "'");
    }

    protected static String doubleQuotes(Object s)
    {
        return s.toString().replace("'", "\"");
    }

    /**
     * Use new ConcatColumnsParser()
     */
    @Deprecated
    public static String resultSetToString(ResultSet rs)
    {
        try
        {
            return new ConcatColumnsParser().parse(rs);
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
    }

    public static DynamicPropertySetSupport getDpsS(Map<String, ?> nameValues)
    {
        return getDps(new DynamicPropertySetSupport(), nameValues);
    }

    public static <T extends DynamicPropertySet> T getDps(T dps, Map<String, ?> nameValues)
    {
        for (Map.Entry<String, ?> entry : nameValues.entrySet())
        {
            dps.add(new DynamicProperty(entry.getKey(), entry.getValue().getClass(), entry.getValue()));
        }
        return dps;
    }

    protected Date parseDate(String stringDate)
    {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        try
        {
            return new Date(df.parse(stringDate).getTime());
        }
        catch (ParseException e)
        {
            throw new RuntimeException(e);
        }
    }

    public static void whenSelectListTagsContains(String containsSql, String... tagValues)
    {
        List<DynamicPropertySet> tagValuesList = Arrays.stream(tagValues)
                .map(tagValue -> getDpsS(ImmutableMap.of("CODE", tagValue, "Name", tagValue)))
                .collect(Collectors.toList());

        when(DbServiceMock.mock.list(contains(containsSql),
                Matchers.<ResultSetParser<DynamicPropertySet>>any(), anyVararg())).thenReturn(tagValuesList);
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
            project = ModuleLoader2.findAndLoadProjectWithModules(false);
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
