package com.developmentontheedge.be5.test;

import com.developmentontheedge.be5.api.services.LoginService;
import com.developmentontheedge.be5.api.services.SqlService;
import com.developmentontheedge.be5.databasemodel.impl.DatabaseModel;
import com.developmentontheedge.be5.env.Inject;
import com.developmentontheedge.be5.env.Injector;
import com.developmentontheedge.be5.env.impl.YamlBinder;
import com.developmentontheedge.be5.maven.AppDb;
import com.developmentontheedge.be5.metadata.model.Project;
import com.developmentontheedge.be5.metadata.util.JULLogger;
import org.apache.maven.plugin.MojoFailureException;
import org.junit.Before;

import java.util.Arrays;
import java.util.Locale;
import java.util.logging.Logger;


public abstract class Be5ProjectDBTest extends TestUtils
{
    private static final Logger log = Logger.getLogger(Be5ProjectDBTest.class.getName());

    private static final Injector injector = initInjector(new YamlBinder());

    @Inject protected DatabaseModel database;
    @Inject protected SqlService db;

    @Before
    public void injectAnnotatedFields()
    {
        injector.injectAnnotatedFields(this);
    }

    protected void initUserWithRoles(String... roles)
    {
        LoginService loginService = injector.get(LoginService.class);
        loginService.saveUser("testUser", Arrays.asList(roles), Locale.US, "", new TestSession());
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
