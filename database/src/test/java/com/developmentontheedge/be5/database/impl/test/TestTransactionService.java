package com.developmentontheedge.be5.database.impl.test;

import com.developmentontheedge.be5.database.DbService;
import com.developmentontheedge.be5.database.Transactional;

import javax.inject.Inject;


public class TestTransactionService
{
    private final DbService db;

    @Inject
    public TestTransactionService(DbService db)
    {
        this.db = db;
    }

    public void testMethod()
    {
        db.insert("INSERT INTO persons (name, password) VALUES (?,?)", "user1", "pass1");

        if (1 == 1)
        {
            throw new RuntimeException();
        }

        db.insert("INSERT INTO persons (name, password) VALUES (?,?)", "user2", "pass2");
    }

    @Transactional
    public void testMethodWithTransactional()
    {
        db.insert("INSERT INTO persons (name, password) VALUES (?,?)", "user1", "pass1");

        if (1 == 1)
        {
            throw new RuntimeException();
        }

        db.insert("INSERT INTO persons (name, password) VALUES (?,?)", "user2", "pass2");
    }
}
