package com.developmentontheedge.be5.test;

import com.developmentontheedge.be5.api.services.ProjectProvider;
import com.developmentontheedge.be5.api.services.SqlService;
import com.developmentontheedge.be5.databasemodel.impl.DatabaseModel;
import com.developmentontheedge.be5.inject.Inject;
import com.developmentontheedge.be5.inject.Injector;
import com.developmentontheedge.be5.inject.impl.YamlBinder;
import com.developmentontheedge.be5.maven.AppDb;
import com.developmentontheedge.be5.metadata.RoleType;
import com.developmentontheedge.be5.metadata.model.Project;
import com.developmentontheedge.be5.metadata.util.JULLogger;
import com.developmentontheedge.be5.api.helpers.UserHelper;
import org.apache.maven.plugin.MojoFailureException;
import org.junit.Before;

import java.io.File;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;


public abstract class Be5ProjectDBTest extends TestUtils
{
    private static final Logger log = Logger.getLogger(Be5ProjectDBTest.class.getName());

    private static final Injector injector = initInjector(new YamlBinder());

    @Inject protected DatabaseModel database;
    @Inject protected SqlService db;

    @Before
    public void setUpBe5ProjectDBTest()
    {
        injector.injectAnnotatedFields(this);
        initGuest();
    }

    protected void initUserWithRoles(String... roles)
    {
        injector.get(UserHelper.class).saveUser(TEST_USER, Arrays.asList(roles), Arrays.asList(roles),
                Locale.US, "", new TestSession());
    }

    protected void initGuest()
    {
        List<String> roles = Collections.singletonList(RoleType.ROLE_GUEST);
        injector.get(UserHelper.class).saveUser(RoleType.ROLE_GUEST, roles, roles,
                Locale.US, "", new TestSession());
    }

    static
    {
        Project project = injector.get(ProjectProvider.class).getProject();

        if(project.getConnectionProfileName() != null &&
                profileForIntegrationTests.equals(project.getConnectionProfileName()))
        {
            try
            {
                File file = Paths.get("target/sql").toFile();
                log.info(JULLogger.infoBlock("Execute be5:create-db"));
                new AppDb()
                        .setLogPath(file)
                        .setLogger(new JULLogger(log))
                        .setBe5Project(project)
                        .execute();

                log.info("Sql log in: " + file.getAbsolutePath());
            }
            catch (MojoFailureException e)
            {
                throw new RuntimeException(e);
            }
        }
        else
        {
            log.warning("Fail set '"+ profileForIntegrationTests +"' profile, maybe DatabaseService already initialized." );
        }
    }

}
