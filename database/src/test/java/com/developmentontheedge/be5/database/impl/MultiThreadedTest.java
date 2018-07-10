package com.developmentontheedge.be5.database.impl;

import com.developmentontheedge.be5.database.test.DatabaseTest;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static junit.framework.TestCase.assertEquals;


public class MultiThreadedTest extends DatabaseTest
{
    @Before
    public void setUp()
    {
        db.update("DELETE FROM persons");
    }

    @Test
    public void testDatabaseService() throws SQLException
    {
        ExecutorService service = Executors.newFixedThreadPool(4);
        for (int i = 0; i < 100; i++)
        {
            service.execute(new Test2());
        }
        service.shutdown();
        try
        {
            service.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }

        assertEquals((Long) 100L, db.oneLong("select count(*) AS \"count\" from Persons"));
    }

    public class Test2 implements Runnable
    {

        @Override
        public void run()
        {
            Random random = new Random();

            db.update("INSERT INTO Persons (name, password)" +
                    "VALUES ('test" + random.nextInt() % 10 + "','" + random.nextInt() + "')");
        }
    }

//    @AfterClass
//    public static void after(){
//        while(databaseService.getNumActive() > 0) {
//            try {
//                Thread.sleep(50);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
//        System.out.println(databaseService.getConnectionsStatistics());
//    }

}
