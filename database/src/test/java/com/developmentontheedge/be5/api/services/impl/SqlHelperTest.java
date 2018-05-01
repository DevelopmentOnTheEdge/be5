package com.developmentontheedge.be5.api.services.impl;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;


public class SqlHelperTest extends Be5ProjectDbBaseTest
{
    private SqlHelper sqlHelper;

    @Before
    public void setUp() throws Exception
    {
        sqlHelper = new SqlHelper(db);
    }

    @Test
    public void update()
    {
        Long id = db.insert("INSERT INTO persons (name, password, email) VALUES (?,?,?)",
                "user2", "pass2", "email2@mail.ru");

        assertEquals(null, db.getInteger("select age from persons where id = ?", id));

        sqlHelper.update("persons", "id", id.toString(), ImmutableMap.of("age", 1));

        assertEquals(1, (int)db.getInteger("select age from persons where id = ?", id));
    }

//    @Test
//    public void generateInsertSql() throws Exception
//    {
//        Entity metersEntity = meta.getEntity("meters")
//
//        String sql = dpsHelper.generateInsertSql(metersEntity, dpsHelper.addDp(dps, metersEntity, [:]))
//        assertEquals "INSERT INTO meters " +
//            "(ID, name, value, whoInserted___, whoModified___, creationDate___, modificationDate___, isDeleted___) " +
//            "VALUES (?, ?, ?, ?, ?, ?, ?, ?)", sql
//    }

}