package com.developmentontheedge.be5.test;

import com.developmentontheedge.be5.api.services.LoginService;
import com.developmentontheedge.be5.env.Be5;
import com.developmentontheedge.be5.env.Binder;
import com.developmentontheedge.be5.env.Injector;
import com.developmentontheedge.be5.metadata.model.BeConnectionProfile;
import com.developmentontheedge.be5.metadata.model.DataElementUtils;
import com.developmentontheedge.be5.metadata.model.Project;
import com.developmentontheedge.be5.metadata.sql.Rdbms;

public abstract class TestUtils
{
    static final String profileForIntegrationTests = "profileForIntegrationTests";

    static Injector initInjector(Binder binder)
    {
        Injector injector = Be5.createInjector(binder);
        Project project = injector.getProject();
        initProfile(project);

        injector.get(LoginService.class).initGuest(null);
        return injector;
    }

    private static void initProfile(Project project)
    {
        if(project.getConnectionProfile() == null || !profileForIntegrationTests.equals(project.getConnectionProfile().getName()))
        {
            BeConnectionProfile profile = new BeConnectionProfile(profileForIntegrationTests, project.getConnectionProfiles().getLocalProfiles());
            profile.setConnectionUrl("jdbc:h2:~/"+ profileForIntegrationTests);
            profile.setUsername("sa");
            profile.setPassword("");
            profile.setDriverDefinition(Rdbms.H2.getDriverDefinition());
            DataElementUtils.save(profile);
            project.setConnectionProfileName(profileForIntegrationTests);
        }
    }

    protected static String oneQuotes(String s)
    {
        return s.replace("\"", "'");
    }

    protected static String doubleQuotes(String s)
    {
        return s.replace("'", "\"");
    }
}
