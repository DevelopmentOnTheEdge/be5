package com.developmentontheedge.be5.maven;

import com.developmentontheedge.be5.metadata.util.NullLogger;
import org.junit.Test;


public class AppDropAllTablesMavenTest extends TestMavenUtils
{
    @Test
    public void createDb()
    {
        AppDropAllTablesMojo mojo = new AppDropAllTablesMojo();
        mojo.projectPath = tpmProjectPath.toFile();
        mojo.connectionProfileName = profileTestMavenPlugin;
        mojo.logger = new NullLogger();
        mojo.execute();
    }
}
