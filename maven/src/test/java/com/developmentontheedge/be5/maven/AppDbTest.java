package com.developmentontheedge.be5.maven;

import org.junit.Test;

import static org.junit.Assert.*;


public class AppDbTest extends TestUtils
{
    @Test
    public void createDb() throws Exception
    {
        new AppDb()
                .setPath(path.toString())
                .setConnectionProfileName(profileTestMavenPlugin)
                .execute();
    }

}