package com.developmentontheedge.be5.maven;

import org.junit.Test;


public class AppDbMavenTest extends TestMavenUtils
{
    @Test
    public void createDb() throws Exception
    {
        AppDbMojo mojo = new AppDbMojo();
        mojo.projectPath = tpmProjectPath.toFile();
        mojo.connectionProfileName = profileTestMavenPlugin;
        mojo.execute();
    }
}
