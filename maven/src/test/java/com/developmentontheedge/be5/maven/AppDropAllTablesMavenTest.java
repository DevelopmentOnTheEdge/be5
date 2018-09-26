package com.developmentontheedge.be5.maven;

import com.developmentontheedge.be5.metadata.scripts.AppDropAllTables;
import org.junit.Test;


public class AppDropAllTablesMavenTest extends TestMavenUtils
{
    @Test
    public void createDb() throws Exception
    {
        AppDropAllTables mojo = new AppDropAllTables();
        mojo.projectPath = tpmProjectPath.toFile();
        mojo.connectionProfileName = profileTestMavenPlugin;
        mojo.execute();
    }
}
