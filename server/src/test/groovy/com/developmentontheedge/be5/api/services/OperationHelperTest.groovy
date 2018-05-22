package com.developmentontheedge.be5.api.services

import com.developmentontheedge.be5.api.helpers.OperationHelper
import com.developmentontheedge.be5.api.sql.DpsRecordAdapter
import javax.inject.Inject
import com.developmentontheedge.be5.model.QRec
import com.developmentontheedge.be5.api.services.databasemodel.DatabaseModel
import com.developmentontheedge.be5.test.ServerBe5ProjectDBTest
import com.developmentontheedge.beans.DynamicPropertySet
import org.junit.Before
import org.junit.Test

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertNotNull


class OperationHelperTest extends ServerBe5ProjectDBTest
{
    @Inject private DbService db
    @Inject private DatabaseModel database
    @Inject private OperationHelper helper

    @Before
    void before()
    {
        database.testtableAdmin.removeAll()
    }

    @Test
    void test()
    {
        String id = database.testtableAdmin << [ "name": "TestName", "value": "1"]

        def rec = helper.qRec("SELECT * FROM testtableAdmin WHERE id = ?", id)

        assertNotNull rec
        assertEquals "TestName", rec.getProperty("name").getValue()
        assertEquals "TestName", rec.$name
    }

    @Test
    void testBeSql()
    {
        String id = database.testtableAdmin << [ "name": "1234567890", "value": "1"]

        assertEquals "10", helper.qRec("SELECT TO_CHAR(LENGTH(name)) FROM testtableAdmin WHERE id = ?", id).getValue()

        assertEquals "10", helper.qRec("SELECT CAST(LEN(name) AS VARCHAR) FROM testtableAdmin WHERE id = ?", id).getValue()
    }

    @Test
    void testGetters()
    {
        String id = database.testtableAdmin << [ "name": "TestName", "value": 123]

        QRec rec = helper.qRec("SELECT * FROM testtableAdmin WHERE id = ?", id)

        if(rec != null)//Can easily check there is no record, when the field can be null
        {
            //One request to the database for several fields
            assertEquals "TestName", rec.getString("name")
            assertEquals 123, rec.getInt("value")
        }

        //use db and DpsRecordAdapter.createDps
        assertEquals "TestName", db.oneString("SELECT name FROM testtableAdmin WHERE id = ?", id)
        assertEquals 123, db.oneInteger("SELECT value FROM testtableAdmin WHERE id = ?", id)

        DynamicPropertySet dps = db.select("SELECT * FROM testtableAdmin WHERE id = ?", { rs -> DpsRecordAdapter.createDps(rs) }, id)
        if(dps != null)
        {
            assertEquals "TestName", dps.getValue("name")
            assertEquals 123, Integer.parseInt(dps.getValue("value").toString())
        }
    }

    @Test
    void testNullIfNoRecords()
    {
        assertEquals null, helper.qRec("SELECT * FROM testtableAdmin WHERE name = ?", "not contain name")
    }


}