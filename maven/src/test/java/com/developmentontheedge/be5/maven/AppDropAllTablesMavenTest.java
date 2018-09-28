package com.developmentontheedge.be5.maven;

import org.junit.Test;


public class AppDropAllTablesMavenTest extends TestMavenUtils
{
    @Test
    public void createDb() throws Exception
    {
        AppDropAllTablesMojo mojo = new AppDropAllTablesMojo();
        mojo.projectPath = tpmProjectPath.toFile();
        mojo.connectionProfileName = profileTestMavenPlugin;
        mojo.execute();
    }
}
