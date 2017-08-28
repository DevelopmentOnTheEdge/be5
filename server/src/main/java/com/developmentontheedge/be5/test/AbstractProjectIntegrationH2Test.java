package com.developmentontheedge.be5.test;

import com.developmentontheedge.be5.api.services.impl.LoginServiceImpl;
import com.developmentontheedge.be5.env.Be5;
import com.developmentontheedge.be5.env.Injector;
import com.developmentontheedge.be5.maven.AppDb;
import com.developmentontheedge.be5.metadata.model.BeConnectionProfile;
import com.developmentontheedge.be5.metadata.model.DataElementUtils;
import com.developmentontheedge.be5.metadata.model.Project;
import com.developmentontheedge.be5.metadata.sql.Rdbms;
import com.developmentontheedge.be5.metadata.util.JULLogger;
import org.apache.maven.plugin.MojoFailureException;

import java.util.Collections;
import java.util.logging.Logger;

public abstract class AbstractProjectIntegrationH2Test extends AbstractProjectTest
{
    protected static final Injector injector = Be5.createInjector();
    private static final Logger log = Logger.getLogger(AbstractProjectIntegrationH2Test.class.getName());

    private static final LoginServiceImpl loginService;

    static {
        Project project = injector.getProject();

        if(project.getConnectionProfile() == null || !profileForIntegrationTests.equals(project.getConnectionProfile().getName()))
        {
            BeConnectionProfile profile = new BeConnectionProfile(profileForIntegrationTests, project.getConnectionProfiles().getLocalProfiles());
            profile.setConnectionUrl("jdbc:h2:~/"+ profileForIntegrationTests);
            profile.setUsername("sa");
            profile.setPassword("");
            profile.setDriverDefinition(Rdbms.H2.getDriverDefinition());
            DataElementUtils.save(profile);
            project.setConnectionProfileName(profileForIntegrationTests);
            log.info(JULLogger.infoBlock("Add and set connection profile for integration tests: " +
                    profileForIntegrationTests));
        }

        if(profileForIntegrationTests.equals(injector.getDatabaseService().getConnectionProfileName()))
        {
            try
            {
                AppDb appDb = new AppDb();
                appDb.setLogger(new JULLogger(Logger.getLogger(AppDb.class.getName())));
                appDb.setBe5Project(project);
                log.info(JULLogger.infoBlock("Execute be5:create-db"));
                appDb.execute();
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

        initProfile(injector.getProject());

        if(project.getProject().getLanguages().length == 0){
            project.getApplication().getLocalizations().addLocalization( "en", "test",
                    Collections.singletonList("myTopic"), "foo", "bar" );
        }

        loginService = new LoginServiceImpl(null, injector.getProjectProvider());
        new LoginServiceImpl(null, injector.getProjectProvider()).initGuest(null);
    }

}
