package com.developmentontheedge.be5.maven;

import com.developmentontheedge.be5.metadata.util.JULLogger;
import org.junit.Test;

import java.util.logging.Logger;


public class AppTests extends TestUtils
{
    @Test
    public void validate() throws Exception
    {
        AppValidate appValidate = new AppValidate();
        appValidate.setLogger(new JULLogger(Logger.getLogger(AppTests.class.getName())));
        appValidate.setPath(path.toAbsolutePath().toString());
        appValidate.execute();
    }

    @Test
    public void createDb() throws Exception
    {
        new AppDb()
                .setPath(path.toString())
                .setConnectionProfileName(profileTestMavenPlugin)
                .execute();
    }

    @Test
    public void sync() throws Exception
    {
        createTestDB();

        new AppSync()
                .setBe5Project(project)
                .setConnectionProfileName(profileTestMavenPlugin)
                .execute();
    }

    @Test
    public void data() throws Exception
    {
        createTestDB();

        new AppData()
                .setBe5Project(project)
                .setConnectionProfileName(profileTestMavenPlugin)
                .execute();
    }

    @Test
    public void dataManyFiles() throws Exception
    {
        createTestDB();

        ((AppData)new AppData().setBe5Project(project).setConnectionProfileName(profileTestMavenPlugin))
            .setScript("all:Post-db;testModule:Post-db;Post-db")
            .execute();
    }

}