package com.developmentontheedge.be5.maven;

import org.junit.Test;


public class AppSyncMavenTest extends TestMavenUtils
{
    @Test
    public void sync() throws Exception
    {
        createTestDB();

        AppSyncMojo mojo = new AppSyncMojo();

        mojo.projectPath = tpmProjectPath.toFile();
        mojo.connectionProfileName = profileTestMavenPlugin;

        mojo.execute();
    }
}