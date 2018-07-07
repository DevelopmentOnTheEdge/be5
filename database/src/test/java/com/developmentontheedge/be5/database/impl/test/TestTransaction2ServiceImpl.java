package com.developmentontheedge.be5.database.impl.test;

import com.developmentontheedge.be5.database.DbService;
import com.developmentontheedge.be5.database.Transactional;

import javax.inject.Inject;

@Transactional
public class TestTransaction2ServiceImpl implements TestTransaction2Service
{
    private final DbService db;

    @Inject
    public TestTransaction2ServiceImpl(DbService db)
    {
        this.db = db;
    }

    @Override
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
