package com.developmentontheedge.be5.test;

import com.developmentontheedge.be5.maven.AppDb;
import com.developmentontheedge.be5.metadata.model.BeConnectionProfile;
import com.developmentontheedge.be5.metadata.model.DataElementUtils;
import com.developmentontheedge.be5.metadata.model.Project;
import com.developmentontheedge.be5.metadata.sql.Rdbms;
import com.developmentontheedge.be5.metadata.util.JULLogger;
import org.apache.maven.plugin.MojoFailureException;

import java.util.logging.Logger;

public abstract class AbstractProjectIntegrationH2Test extends AbstractProjectTest
{
    private static final Logger log = Logger.getLogger(AbstractProjectIntegrationH2Test.class.getName());

    private static final String profileForIntegrationTests = "profileForIntegrationTests";

    static {
        Project project = injector.getProject().getProject();

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
    }

}
