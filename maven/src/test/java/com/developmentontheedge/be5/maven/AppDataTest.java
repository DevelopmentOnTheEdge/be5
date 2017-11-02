package com.developmentontheedge.be5.maven;

import org.junit.Test;

public class AppDataTest extends TestUtils
{
    @Test
    public void data() throws Exception
    {
        createTestDB();

        new AppData()
                .setBe5Project(project)
                .setProfileName(profileTestMavenPlugin)
                .execute();
    }

    @Test
    public void dataManyFiles() throws Exception
    {
        createTestDB();

        new AppData()
                .setBe5Project(project)
                .setProfileName(profileTestMavenPlugin)
                .setScript("all:Post-db;testModule:Post-db;Post-db")
                .execute();
    }
}