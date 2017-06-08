package com.developmentontheedge.be5.test;

import com.developmentontheedge.be5.api.services.SqlService;
import com.developmentontheedge.be5.metadata.model.BeConnectionProfile;
import com.developmentontheedge.be5.metadata.model.DataElementUtils;
import com.developmentontheedge.be5.metadata.model.DdlElement;
import com.developmentontheedge.be5.metadata.model.Entity;
import com.developmentontheedge.be5.metadata.model.Module;
import com.developmentontheedge.be5.metadata.model.Project;
import com.developmentontheedge.be5.metadata.model.TableDef;
import com.developmentontheedge.be5.metadata.sql.Rdbms;

import java.util.logging.Logger;

public abstract class AbstractProjectIntegrationH2Test extends AbstractProjectTest
{
    private static final Logger log = Logger.getLogger(AbstractProjectIntegrationH2Test.class.getName());

    static {
        createTablesInH2();
    }

    private static void createTablesInH2()
    {
        Project project = injector.getProject().getProject();

        String profileForIntegrationTests = "profileForIntegrationTests";

        if(project.getConnectionProfile() == null || !profileForIntegrationTests.equals(project.getConnectionProfile().getName()))
        {
            BeConnectionProfile profile = new BeConnectionProfile(profileForIntegrationTests, project.getConnectionProfiles().getLocalProfiles());
            profile.setConnectionUrl("jdbc:h2:~/"+ profileForIntegrationTests);
            profile.setUsername("sa");
            profile.setPassword("");
            profile.setDriverDefinition(Rdbms.H2.getDriverDefinition());
            DataElementUtils.save(profile);
            project.setConnectionProfileName(profileForIntegrationTests);

            log.info("Set connection profile for integration tests: " + profileForIntegrationTests);
        }

        if("profileForIntegrationTests".equals(injector.getDatabaseService().getConnectionProfileName()))
        {
            Module application = injector.getProject().getApplication();
            SqlService db = injector.getSqlService();

            //todo duplicate code from: be5:create-db, fix it
            log.info("Drop and create application tables.");
            for(Entity entity : application.getOrCreateEntityCollection().getAvailableElements())
            {
                DdlElement scheme = entity.getScheme();
                if(scheme instanceof TableDef)
                {
                    final String generatedQuery = scheme.getDdl();
                    db.update( generatedQuery );
                }
            }
        }
        else
        {
            log.warning("Fail set '"+ profileForIntegrationTests +"' profile, maybe DatabaseService already initialized." );
        }

    }
}
