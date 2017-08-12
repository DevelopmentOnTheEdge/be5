package com.developmentontheedge.be5.api.services.impl;

import com.developmentontheedge.be5.test.AbstractProjectTest;
import com.developmentontheedge.be5.api.services.DatabaseService;
import com.developmentontheedge.be5.api.services.SqlService;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.SQLException;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static junit.framework.TestCase.assertEquals;


public class MultiThreadedTest extends AbstractProjectTest
{
    private static SqlService db;
    private static DatabaseService databaseService;

    @BeforeClass
    public static void setUp() throws Exception
    {
        databaseService = injector.getDatabaseService();
        db = injector.getSqlService();
        db.update("DELETE FROM persons" );
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

        assertEquals((Long)100L, db.getLong("select count(*) AS \"count\" from Persons"));
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
