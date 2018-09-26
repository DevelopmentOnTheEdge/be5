package com.developmentontheedge.be5.metadata.scripts;

import org.junit.Test;

public class AppDataTest extends ScriptTestUtils
{
    @Test
    public void data() throws Exception
    {
        dropAndCreateTestDB();

        new AppData()
                .setBe5Project(project)
                .setProfileName(profileTestMavenPlugin)
                .execute();
    }

    @Test
    public void dataManyFiles() throws Exception
    {
        dropAndCreateTestDB();

        new AppData()
                .setBe5Project(project)
                .setProfileName(profileTestMavenPlugin)
                .setScript("all:Post-db;testModule:Post-db;Post-db")
                .execute();
    }
}
