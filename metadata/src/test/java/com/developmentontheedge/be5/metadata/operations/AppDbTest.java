package com.developmentontheedge.be5.metadata.operations;

import org.junit.Test;


public class AppDbTest extends TestUtils
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