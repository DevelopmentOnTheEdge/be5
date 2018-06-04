package com.developmentontheedge.be5.metadata.scripts;

import org.junit.Test;


public class AppDbTest extends ScriptTestUtils
{
    @Test
    public void createDb() throws Exception
    {
        new AppDb()
                .setBe5ProjectPath(tpmProjectPath.toString())
                .setProfileName(profileTestMavenPlugin)
                .execute();
    }

}