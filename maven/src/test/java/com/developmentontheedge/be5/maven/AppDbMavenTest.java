package com.developmentontheedge.be5.maven;

import com.developmentontheedge.be5.metadata.scripts.AppDropAllTables;
import org.junit.Before;
import org.junit.Test;


public class AppDbMavenTest extends TestMavenUtils
{
    @Before
    public void createDbSetUp() throws Exception
    {
        new AppDropAllTables()
                .setBe5ProjectPath(tpmProjectPath.toFile().toPath())
                .setProfileName(profileTestMavenPlugin)
                .execute();
    }

    @Test
    public void createDb() throws Exception
    {
        AppDbMojo mojo = new AppDbMojo();
        mojo.projectPath = tpmProjectPath.toFile();
        mojo.connectionProfileName = profileTestMavenPlugin;
        mojo.execute();
    }
}
