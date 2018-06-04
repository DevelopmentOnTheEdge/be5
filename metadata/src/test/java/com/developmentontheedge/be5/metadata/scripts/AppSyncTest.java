package com.developmentontheedge.be5.metadata.scripts;

import org.junit.Test;


public class AppSyncTest extends ScriptTestUtils
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