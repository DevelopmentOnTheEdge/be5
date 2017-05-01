package com.developmentontheedge.be5.api.services.impl;

import com.developmentontheedge.be5.test.AbstractProjectTest;
import com.developmentontheedge.be5.api.exceptions.Be5Exception;
import com.developmentontheedge.be5.api.services.DatabaseService;
import com.developmentontheedge.be5.api.services.SqlService;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;


import static org.junit.Assert.assertEquals;


public class TransactionTest extends AbstractProjectTest
{
    private static SqlService db;

    private static DatabaseService databaseService;

    @BeforeClass
    public static void setUp()
    {
        databaseService = sp.getDatabaseService();
        db = sp.getSqlService();
        db.update("DROP TABLE IF EXISTS persons;" );
        db.update("CREATE TABLE persons (\n" +
                "    ID  BIGSERIAL PRIMARY KEY,\n" +
                "    name varchar(255),\n" +
                "    password varchar(255),\n" +
                "    email varchar(255) \n" +
                ");");
    }

    @Test
    public void testSimple() {
        databaseService.transaction(conn -> {
            db.update("INSERT INTO persons (name, password) VALUES (?,?)","user1", "pass1");
            db.update("INSERT INTO persons (name, password) VALUES (?,?)","user12", "pass2");
            return null;//TODO сделаем возможность запросов без возвращения результата (аналог Spring TransactionCallbackWithoutResult) https://habrahabr.ru/post/183204/
        });
        long countUser1 = db.selectScalar("SELECT count(*) FROM persons WHERE name LIKE 'user1%'" );
        assertEquals(2, countUser1);
    }

    @Test
    public void testSimpleError() {
        try {
            databaseService.transaction(conn -> {
                db.update("INSERT INTO persons (name, password) VALUES (?,?)", "userError","pass1");
                db.update("INSERT INTO persons (name, password2) VALUES (?,?)", "user2","pass1");
                return null;
            });
            Assert.fail("Should have thrown Be5Exception");
        }
        catch (Be5Exception e) {
            Assert.assertTrue(true);
            long countUserError = db.selectScalar("SELECT count(*) FROM persons WHERE name = 'userError'" );
            assertEquals(0, countUserError);
        }
    }

}
