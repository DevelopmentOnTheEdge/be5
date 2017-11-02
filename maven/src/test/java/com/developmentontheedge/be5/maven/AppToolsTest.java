package com.developmentontheedge.be5.maven;

import org.junit.Before;
import org.junit.Test;


public class AppToolsTest extends TestUtils
{
    private AppTools appTools;

    @Before
    public void setUpAppTools() throws Exception
    {
        createTestDB();

        appTools = new AppTools();
        appTools
                .setBe5Project(project)
                .setConnectionProfileName(profileTestMavenPlugin)
                .setPath(path.toAbsolutePath().toString());
    }

    @Test
    public void sql() throws Exception
    {
        appTools
                .setInputStream("select * from entity")
                .execute();
    }

    @Test
    public void error() throws Exception
    {
        appTools
                .setInputStream("select * from entityError")
                .execute();
    }

    @Test
    public void ftl() throws Exception
    {
        appTools
                .setInputStream("//${concat('a'?asDate, 'b', 'c'?str)}")
                .execute();
    }
}