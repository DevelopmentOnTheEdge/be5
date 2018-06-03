package com.developmentontheedge.be5.metadata.operations;

import org.junit.Test;


public class AppSyncTest extends TestUtils
{
    @Test
    public void sync() throws Exception
    {
        createTestDB();

        new AppSync()
                .setBe5Project(project)
                .setProfileName(profileTestMavenPlugin)
                .execute();
    }
}