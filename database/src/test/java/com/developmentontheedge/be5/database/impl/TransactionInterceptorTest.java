package com.developmentontheedge.be5.database.impl;

import com.developmentontheedge.be5.database.impl.test.TestTransaction2Service;
import com.developmentontheedge.be5.database.impl.test.TestTransactionService;
import com.developmentontheedge.be5.database.DatabaseTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.Ignore;

import javax.inject.Inject;

import static org.junit.Assert.*;


public class TransactionInterceptorTest extends DatabaseTest
{
    @Inject
    private TestTransactionService testTransactionService;
    @Inject
    private TestTransaction2Service testTransaction2Service;

    @Before
    public void setUp()
    {
        db.update("DELETE FROM persons");
    }

    @Test
    public void errorExample()
    {
        try
        {
            testTransactionService.testMethod();
        }       	
        catch (RuntimeException ignore)
        {
        }

        assertEquals(1, db.countFrom("SELECT count(*) FROM persons WHERE name LIKE 'user1%'"));
    }

    @Test
    public void useTransactional()
    {
        try
        {
            testTransactionService.testMethodWithTransactional();
        }
        catch (RuntimeException ignore)
        {
        }

        assertEquals(0, db.countFrom("SELECT count(*) FROM persons WHERE name LIKE 'user1%'"));
    }

    @Test
    public void useTransactionalWithInterface()
    {
        try
        {
            testTransaction2Service.testMethodWithTransactional();
        }
        catch (RuntimeException ignore)
        {
        }

        assertEquals(0, db.countFrom("SELECT count(*) FROM persons WHERE name LIKE 'user1%'"));
    }
}
