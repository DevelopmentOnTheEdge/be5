package com.developmentontheedge.be5.maven;

import org.junit.Before;
import org.junit.Test;


public class AppToolsMavenTest extends TestMavenUtils
{
    private AppToolsMojo mojo;

    @Before
    public void setUpAppTools() throws Exception
    {
        createTestDB();

        mojo = new AppToolsMojo();

        mojo.projectPath = tpmProjectPath.toFile();
        mojo.connectionProfileName = profileTestMavenPlugin;
    }

    @Test
    public void sql() throws Exception
    {
        mojo.inputStream = inputStream("select * from entity");

        mojo.execute();
    }
}
