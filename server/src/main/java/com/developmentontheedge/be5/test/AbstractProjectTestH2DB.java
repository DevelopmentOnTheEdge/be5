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

public abstract class AbstractProjectTestH2DB extends AbstractProjectTest
{
    static {
        createTablesInH2();
    }

    public static void createTablesInH2()
    {
        Project project = sp.getProject().getProject();

        if(project.getConnectionProfile() == null || !"testProfile".equals(project.getConnectionProfile().getName()))
        {
            BeConnectionProfile profile = new BeConnectionProfile("testProfile", project.getConnectionProfiles().getLocalProfiles());
            profile.setConnectionUrl("jdbc:h2:~/testBe5");
            profile.setUsername("sa");
            profile.setPassword("");
            profile.setDriverDefinition(Rdbms.H2.getDriverDefinition());
            DataElementUtils.save(profile);
            project.setConnectionProfileName("testProfile");
        }

        Module application = sp.getProject().getApplication();
        SqlService db = sp.getSqlService();

        //todo remove duplicate code
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
}
