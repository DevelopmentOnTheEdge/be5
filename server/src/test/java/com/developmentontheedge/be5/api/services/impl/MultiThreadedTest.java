package com.developmentontheedge.be5.api.services.impl;

import com.developmentontheedge.be5.AbstractProjectTest;
import com.developmentontheedge.be5.api.services.DatabaseService;
import com.developmentontheedge.be5.api.services.SqlService;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;


public class MultiThreadedTest extends AbstractProjectTest
{
    private static SqlService db;
    private static DatabaseService databaseService;

    @BeforeClass
    public static void setUp() throws Exception
    {
        databaseService = getServiceProvider().getDatabaseService();
        db = getServiceProvider().getSqlService();
        db.update("DROP TABLE IF EXISTS Persons" );
        db.update("CREATE TABLE Persons (\n" +
                "    ID int NOT NULL AUTO_INCREMENT,\n" +
                "    name varchar(255),\n" +
                "    password varchar(255),\n" +
                "    email varchar(255) \n" +
                ")");
    }

    @Test
    public void testDatabaseService() throws SQLException {
        ExecutorService service = Executors.newFixedThreadPool(4);
        for (int i = 0; i < 100; i++) {
            service.execute(new Test2());
        }
        service.shutdown();
        try {
            service.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        long count = db.selectScalar("select count(*) AS \"count\" from Persons");

        assertEquals(100, count);
    }

    public class Test2 implements Runnable{

        @Override
        public void run()  {
            Random random = new Random();

            db.update("INSERT INTO Persons (name, password)" +
                        "VALUES ('test" + random.nextInt()%10 + "','" + random.nextInt() + "')");
        }
    }

    @AfterClass
    public static void after(){
        while(databaseService.getNumActive() > 0) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println(databaseService.getConnectionsStatistics());
    }

}
