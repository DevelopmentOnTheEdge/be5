package com.developmentontheedge.be5.database.impl;

import com.developmentontheedge.be5.database.ConnectionService;
import com.developmentontheedge.be5.database.test.DatabaseTest;
import org.junit.Before;
import org.junit.Test;

import javax.inject.Inject;
import java.sql.Connection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.Assert.assertEquals;

public class ConnectionServiceTest extends DatabaseTest
{
    @Inject
    private ConnectionService connectionService;

    @Before
    public void setUp()
    {
        db.update("DELETE FROM persons");
    }

    @Test
    public void test() throws Exception
    {
        assertEquals(null, connectionService.getCurrentTxConn());

        Connection connection = connectionService.beginTransaction();
        assertEquals(connection, connectionService.getCurrentTxConn());

        db.insert("INSERT INTO persons (name, password) VALUES (?,?)", "test", "pass");
        connectionService.endTransaction();

        assertEquals(1L, (long)db.oneLong("SELECT count(*) FROM persons"));
        checkCountInOtherThreads(1L);
    }

    @Test
    public void nextTransactionAfterErrorInTransaction() throws Exception
    {
        try{
            connectionService.transaction(connection ->
                connectionService.rollbackTransaction()
            );
        }catch (RuntimeException ignore){}

        connectionService.transaction(conn ->
            db.insert("INSERT INTO persons (name, password) VALUES (?,?)", "test", "pass")
        );

        assertEquals(1L, (long)db.oneLong("SELECT count(*) FROM persons"));
        checkCountInOtherThreads(1L);
    }

    @Test
    public void checkInOtherThreadsBeforeCommit() throws Exception
    {
        connectionService.transaction(conn -> {
            db.insert("INSERT INTO persons (name, password) VALUES (?,?)", "test", "pass");
            assertEquals(1L, (long)db.oneLong("SELECT count(*) FROM persons"));
            checkCountInOtherThreads(0L);
        });

        assertEquals(1L, (long)db.oneLong("SELECT count(*) FROM persons"));
        checkCountInOtherThreads(1L);
    }

    private void checkCountInOtherThreads(Long count) throws InterruptedException, ExecutionException
    {
        Set<Future<Long>> set = new HashSet<>();
        ExecutorService service = Executors.newFixedThreadPool(4);
        for (int i = 0; i < 10; i++)
        {
            Future<Long> future = service.submit(() ->
                db.oneLong("SELECT count(*) FROM persons")
            );
            set.add(future);
        }

        for (Future<Long> future : set) {
            assertEquals(count, future.get());
        }
        service.shutdown();
    }

}
