package com.developmentontheedge.be5.maven;

import org.junit.Test;


public class AppDbTest extends TestUtils
{
    @Test
    public void createDb() throws Exception
    {
        new AppDb()
                .setProjectPath(path.toString())
                .setProfileName(profileTestMavenPlugin)
                .execute();
    }

}