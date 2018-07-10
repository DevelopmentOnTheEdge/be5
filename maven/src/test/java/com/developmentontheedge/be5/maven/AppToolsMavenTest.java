package com.developmentontheedge.be5.maven;

import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;


public class AppToolsMavenTest extends TestMavenUtils
{
    private AppToolsMojo mojo;

    @Before
    public void setUpAppTools() throws Exception
    {
        createTestDB();

        mojo = new AppToolsMojo();

        mojo.projectPath = tpmProjectPath.toFile();
        mojo.connectionProfileName = profileTestMavenPlugin;
    }

    @Test
    public void sql() throws Exception
    {
        mojo.inputStream = inputStream("select * from entity");

        mojo.execute();
    }

    public InputStream inputStream(String str)
    {
        try
        {
            return new ByteArrayInputStream(str.getBytes(StandardCharsets.UTF_8.name()));
        }
        catch (UnsupportedEncodingException e)
        {
            throw new RuntimeException(e);
        }
    }
}