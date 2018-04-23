package com.developmentontheedge.be5.api.services.impl;

import com.developmentontheedge.be5.api.services.ProjectProvider;
import com.developmentontheedge.be5.env.Injector;
import com.developmentontheedge.be5.env.Stage;
import com.developmentontheedge.be5.env.impl.Be5Injector;
import com.developmentontheedge.be5.env.impl.YamlBinder;
import com.developmentontheedge.be5.maven.AppDb;
import com.developmentontheedge.be5.metadata.model.Project;
import com.developmentontheedge.be5.metadata.util.JULLogger;
import org.apache.maven.plugin.MojoFailureException;
import org.junit.Before;

import java.util.logging.Logger;


public abstract class Be5ProjectDbBaseTest
{
    private static final Injector injector = new Be5Injector(Stage.TEST, new YamlBinder());

    @Before
    public void setUpBe5ProjectDBTest()
    {
        injector.injectAnnotatedFields(this);
    }

    static
    {
        Project project = injector.get(ProjectProvider.class).getProject();

        try
        {
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

}
