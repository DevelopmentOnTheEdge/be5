package com.developmentontheedge.be5.metadata.scripts;

import org.junit.Test;


public class AppDbTest extends ScriptTestUtils
{
    @Test
    public void createDb() throws Exception
    {
        AppDropAllTables appDropAllTables = new AppDropAllTables();
        appDropAllTables.setBe5Project(project)
                .setProfileName(profileTestMavenPlugin)
                .execute();

        new AppDb()
                .setBe5ProjectPath(tpmProjectPath.toString())
                .setProfileName(profileTestMavenPlugin)
                .execute();
    }

}
