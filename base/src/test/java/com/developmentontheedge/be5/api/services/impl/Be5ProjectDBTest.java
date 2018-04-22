package com.developmentontheedge.be5.api.services.impl;

import com.developmentontheedge.be5.api.services.DatabaseService;
import com.developmentontheedge.be5.api.services.ProjectProvider;
import com.developmentontheedge.be5.api.services.SqlService;
import com.developmentontheedge.be5.env.Binder;
import com.developmentontheedge.be5.env.Inject;
import com.developmentontheedge.be5.env.Injector;
import com.developmentontheedge.be5.env.Stage;
import com.developmentontheedge.be5.env.impl.Be5Injector;
import com.developmentontheedge.be5.env.impl.YamlBinder;
import com.developmentontheedge.be5.maven.AppDb;
import com.developmentontheedge.be5.metadata.model.Project;
import com.developmentontheedge.be5.metadata.util.JULLogger;
import com.developmentontheedge.be5.metadata.util.ProjectTestUtils;
import org.apache.maven.plugin.MojoFailureException;
import org.junit.Before;

import java.util.logging.Logger;


public abstract class Be5ProjectDBTest
{
    private static final Logger log = Logger.getLogger(Be5ProjectDBTest.class.getName());

    private static final Injector injector = initInjector(new YamlBinder());

    @Inject protected SqlService db;

    @Before
    public void setUpBe5ProjectDBTest()
    {
        injector.injectAnnotatedFields(this);
    }

    private static final String profileForIntegrationTests = "profileForIntegrationTests";

    static Injector initInjector(Binder binder)
    {
        Injector injector = new Be5Injector(Stage.TEST, binder);
        Project project = injector.get(ProjectProvider.class).getProject();
        initProfile(project);

        return injector;
    }

    private static void initProfile(Project project)
    {
        if(project.getConnectionProfile() == null || !profileForIntegrationTests.equals(project.getConnectionProfile().getName()))
        {
            ProjectTestUtils.createH2Profile(project, profileForIntegrationTests);
            project.setConnectionProfileName(profileForIntegrationTests);
        }
    }

    static
    {
        Project project = injector.get(ProjectProvider.class).getProject();

        if(injector.get(DatabaseService.class).getConnectionProfileName() != null &&
                profileForIntegrationTests.equals(injector.get(DatabaseService.class).getConnectionProfileName()))
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
