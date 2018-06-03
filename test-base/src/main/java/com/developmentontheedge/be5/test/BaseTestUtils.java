package com.developmentontheedge.be5.test;

import com.developmentontheedge.be5.base.model.UserInfo;
import com.developmentontheedge.be5.base.services.UserInfoProvider;
import com.developmentontheedge.be5.base.services.UserAwareMeta;
import com.developmentontheedge.be5.base.services.Be5Caches;
import com.developmentontheedge.be5.database.ConnectionService;
import com.developmentontheedge.be5.database.DataSourceService;
import com.developmentontheedge.be5.database.DbService;
import com.developmentontheedge.be5.base.services.Meta;
import com.developmentontheedge.be5.base.services.ProjectProvider;
import com.developmentontheedge.be5.database.sql.ResultSetParser;
import com.developmentontheedge.be5.metadata.model.Project;
import com.developmentontheedge.be5.metadata.operations.AppDb;
import com.developmentontheedge.be5.metadata.operations.DatabaseTargetException;
import com.developmentontheedge.be5.metadata.util.JULLogger;
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
import org.apache.maven.plugin.MojoFailureException;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static com.developmentontheedge.be5.test.TestProjectProvider.profileForIntegrationTests;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Matchers.contains;
import static org.mockito.Mockito.when;


public abstract class BaseTestUtils
{
    public static final Logger log = Logger.getLogger(BaseTestUtils.class.getName());

    protected static final Jsonb jsonb = JsonbBuilder.create();

    @Inject protected Meta meta;
    @Inject protected UserAwareMeta userAwareMeta;
    @Inject protected DbService db;

    @Inject protected UserInfoProvider userInfoProvider;

    protected static final String TEST_USER = "testUser";
    //protected static final Jsonb jsonb = JsonbBuilder.create();

    @Before
    public void setUpBaseTestUtils()
    {
        if(getInjector() != null)
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
        return Guice.createInjector(Stage.DEVELOPMENT, modules);
    }

    protected static String oneQuotes(Object s)
    {
        return s.toString().replace("\"", "'");
    }

    protected static String doubleQuotes(Object s)
    {
        return s.toString().replace("'", "\"");
    }

    public static String resultSetToString(ResultSet rs) {
        List<String> list = new ArrayList<>();
        try {
            for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                if(rs.getObject(i) != null)
                    list.add(rs.getObject(i).toString());
                else{
                    list.add("null");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list.stream().collect(Collectors.joining(","));
    }

    public static DynamicPropertySetSupport getDpsS(Map<String, Object> nameValues)
    {
        return getDps(new DynamicPropertySetSupport(), nameValues);
    }

    public static <T extends DynamicPropertySet> T getDps(T dps, Map<String, Object> nameValues)
    {
        for(Map.Entry<String, Object> entry : nameValues.entrySet())
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
                .map(tagValue -> getDpsS(ImmutableMap.of("CODE", tagValue, "Name", tagValue))).collect(Collectors.toList());

        when(DbServiceMock.mock.list(contains(containsSql),
                Matchers.<ResultSetParser<DynamicPropertySet>>any(), anyVararg())).thenReturn(tagValuesList);
    }

    protected static void initDb(Injector injector)
    {
        Project project = injector.getInstance(ProjectProvider.class).getProject();

        if(project.getConnectionProfileName() != null &&
                profileForIntegrationTests.equals(project.getConnectionProfileName()))
        {
            log.info(JULLogger.infoBlock("Execute be5:create-db"));
            new AppDb()
                    .setLogger(new JULLogger(log))
                    .setBe5Project(project)
                    .execute();
        }
        else
        {
            log.warning("Fail set '"+ profileForIntegrationTests +"' profile, maybe DatabaseService already initialized." );
        }
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
