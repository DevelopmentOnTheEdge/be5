package com.developmentontheedge.be5.maven;

import org.junit.Test;

public class AppDataTest extends TestUtils
{
    @Test
    public void data() throws Exception
    {
        createTestDB();

        AppDataMojo mojo = new AppDataMojo();

        mojo.projectPath = tpmProjectPath.toFile();
        mojo.connectionProfileName = profileTestMavenPlugin;
        mojo.script = "all:Post-db;testModule:Post-db;Post-db";
        mojo.ignoreMissing = false;

        mojo.execute();
    }

}