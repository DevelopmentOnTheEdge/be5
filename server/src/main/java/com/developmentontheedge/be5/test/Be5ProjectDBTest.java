package com.developmentontheedge.be5.test;

import com.developmentontheedge.be5.api.services.SqlService;
import com.developmentontheedge.be5.databasemodel.impl.DatabaseModel;
import com.developmentontheedge.be5.env.Inject;
import com.developmentontheedge.be5.env.Injector;
import com.developmentontheedge.be5.env.impl.YamlBinder;
import com.developmentontheedge.be5.maven.AppDb;
import com.developmentontheedge.be5.metadata.RoleType;
import com.developmentontheedge.be5.metadata.model.Project;
import com.developmentontheedge.be5.metadata.util.JULLogger;
import com.developmentontheedge.be5.api.helpers.UserHelper;
import org.apache.maven.plugin.MojoFailureException;
import org.junit.Before;

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
        Project project = injector.getProject();

        if(injector.getDatabaseService().getConnectionProfileName() != null &&
                profileForIntegrationTests.equals(injector.getDatabaseService().getConnectionProfileName()))
        {
            try
            {
                log.info(JULLogger.infoBlock("Execute be5:create-db"));
                new AppDb()
                        .setLogger(new JULLogger(Logger.getLogger(AppDb.class.getName())))
                        .setBe5Project(project)
                        .execute();
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
