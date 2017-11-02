package com.developmentontheedge.be5.maven;

import org.junit.Test;

import static org.junit.Assert.*;

public class AppDataTest extends TestUtils
{
    @Test
    public void data() throws Exception
    {
        createTestDB();

        new AppData()
                .setBe5Project(project)
                .setConnectionProfileName(profileTestMavenPlugin)
                .execute();
    }

    @Test
    public void dataManyFiles() throws Exception
    {
        createTestDB();

        new AppData()
                .setBe5Project(project)
                .setConnectionProfileName(profileTestMavenPlugin)
                .setScript("all:Post-db;testModule:Post-db;Post-db")
                .execute();
    }
}