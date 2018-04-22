package com.developmentontheedge.be5.api.services.impl;

import com.developmentontheedge.be5.env.Inject;
import com.developmentontheedge.be5.api.exceptions.Be5Exception;
import com.developmentontheedge.be5.api.services.SqlService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;


public class DatabaseServiceTransactionTest extends Be5ProjectDBTest
{
    @Inject private SqlService db;

    @Before
    public void setUp()
    {
        db.update("DELETE FROM persons" );
    }

    @Test
    public void testSimple()
    {
        db.transaction(conn -> {
            db.insert("INSERT INTO persons (name, password) VALUES (?,?)","user1", "pass1");
            db.insert("INSERT INTO persons (name, password) VALUES (?,?)","user12", "pass2");
        });
        long countUser1 = db.getScalar("SELECT count(*) FROM persons WHERE name LIKE 'user1%'" );
        assertEquals(2, countUser1);
    }

    @Test
    public void testSimpleError()
    {
        try {
            db.transaction(conn -> {
                db.insert("INSERT INTO persons (name, password) VALUES (?,?)", "user1","pass1");
                throw new RuntimeException("test rollback");
            });
            Assert.fail("Should have thrown Be5Exception");
        }
        catch (Be5Exception e) {
            Assert.assertTrue(true);
            assertEquals(0L, (long)db.getLong("SELECT count(*) FROM persons" ));
        }
    }

    @Test
    public void testErrorInInnerTransaction()
    {
        try {
            db.transaction(conn -> {
                db.insert("INSERT INTO persons (name, password) VALUES (?,?)", "user1","pass1");

                db.transaction(conn2 -> {
                    db.insert("INSERT INTO persons (name, password) VALUES (?,?)", "user2","pass2");
                });

                throw new RuntimeException("test rollback");
            });
            Assert.fail("Should have thrown Be5Exception");
        }
        catch (Be5Exception e) {
            Assert.assertTrue(true);
            assertEquals(0L, (long)db.getLong("SELECT count(*) FROM persons" ));
        }
    }
}
