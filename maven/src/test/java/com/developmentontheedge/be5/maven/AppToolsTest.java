package com.developmentontheedge.be5.maven;

import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.*;


public class AppToolsTest extends TestUtils
{
    private AppTools appTools;

    @Before
    public void setUpAppTools() throws Exception
    {
        createTestDB();

        appTools = new AppTools();
        appTools.setBe5Project(project);
        appTools.setPath(path.toAbsolutePath().toString());
    }

    @Test
    public void sql() throws Exception
    {
        String commandString = "select * from entity";
        appTools.inputStream = new ByteArrayInputStream(commandString.getBytes(StandardCharsets.UTF_8.name()));
        appTools.execute();
    }

    @Test
    public void error() throws Exception
    {
        String commandString = "select * from entityError";
        appTools.inputStream = new ByteArrayInputStream(commandString.getBytes(StandardCharsets.UTF_8.name()));
        appTools.execute();
    }

    @Test
    public void ftl() throws Exception
    {
        String commandString = "//${concat('a'?asDate, 'b', 'c'?str)}";
        appTools.inputStream = new ByteArrayInputStream(commandString.getBytes(StandardCharsets.UTF_8.name()));
        appTools.execute();
    }
}