package com.developmentontheedge.be5.database.impl;

import com.developmentontheedge.be5.database.DatabaseTest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;


public class DatabaseServiceTransactionTest extends DatabaseTest
{
    @Before
    public void setUp()
    {
        db.update("DELETE FROM persons");
    }

    @Test
    public void testSimple()
    {
        long countUser1 = db.inTransaction(conn -> {
            db.insert("INSERT INTO persons (name, password) VALUES (?,?)", "user1", "pass1");
            db.insert("INSERT INTO persons (name, password) VALUES (?,?)", "user12", "pass2");

            return db.one("SELECT count(*) FROM persons WHERE name LIKE 'user1%'");
        });

        assertEquals(2, countUser1);
    }

    @Test
    public void testSimpleError()
    {
        try
        {
            db.useTransaction(conn -> {
                db.insert("INSERT INTO persons (name, password) VALUES (?,?)", "user1", "pass1");
                throw new RuntimeException("test rollback");
            });
            Assert.fail("Should have thrown Be5Exception");
        }
        catch (RuntimeException e)
        {
            Assert.assertTrue(true);
            assertEquals(0L, db.countFrom("persons"));
        }
    }

    @Test
    public void testErrorWithInnerTransaction()
    {
        try
        {
            db.useTransaction(conn -> {
                db.useTransaction(conn2 ->
                        db.insert("INSERT INTO persons (name, password) VALUES (?,?)", "user2", "pass2")
                );

                db.insert("INSERT INTO persons (name, password) VALUES (?,?)", "user1", "pass1");

                throw new RuntimeException("test rollback");
            });
            Assert.fail("Should have thrown Be5Exception");
        }
        catch (RuntimeException e)
        {
            Assert.assertTrue(true);
            assertEquals(0L, db.countFrom("persons"));
        }
    }

    @Test
    public void testErrorInInnerTransaction()
    {
        try
        {
            db.useTransaction(conn -> {
                db.insert("INSERT INTO persons (name, password) VALUES (?,?)", "user1", "pass1");

                db.useTransaction(conn2 -> {
                    db.insert("INSERT INTO persons (name, password) VALUES (?,?)", "user2", "pass2");
                    throw new RuntimeException("test rollback");
                });
            });
            Assert.fail("Should have thrown Be5Exception");
        }
        catch (RuntimeException e)
        {
            Assert.assertTrue(true);
            assertEquals(0L, db.countFrom("persons"));
        }
    }
}
