package com.developmentontheedge.be5.maven;

import org.junit.Test;

import static org.junit.Assert.*;


public class AppSyncTest extends TestUtils
{
    @Test
    public void sync() throws Exception
    {
        createTestDB();

        new AppSync()
                .setBe5Project(project)
                .setConnectionProfileName(profileTestMavenPlugin)
                .execute();
    }
}