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

//    @Test
//    public void createDb() throws Exception
//    {
//        createTestDB();
//
//        AppSync appSync = new AppSync();
//        appSync.setBe5Project(project);
//        appSync.execute();
//    }

    @Test
    public void sync() throws Exception
    {
        createTestDB();

        AppSync appSync = new AppSync();
        appSync.setBe5Project(project);
        appSync.execute();
    }

    @Test
    public void data() throws Exception
    {
        createTestDB();

        AppData appData = new AppData();
        appData.setBe5Project(project);
        appData.execute();
    }

    @Test
    public void dataManyFiles() throws Exception
    {
        createTestDB();

        AppData appData = new AppData();
        appData.setBe5Project(project);
        appData.setScript("all:Post-db;testModule:Post-db;Post-db");//todo add module and
        appData.execute();
    }

}