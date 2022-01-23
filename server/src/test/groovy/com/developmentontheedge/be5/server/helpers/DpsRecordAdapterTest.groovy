package com.developmentontheedge.be5.server.helpers

import com.developmentontheedge.be5.databasemodel.DatabaseModel
import com.developmentontheedge.be5.test.ServerBe5ProjectDBTest
import org.junit.Before
import org.junit.Test

import javax.inject.Inject

import static org.junit.Assert.assertEquals


class DpsRecordAdapterTest extends ServerBe5ProjectDBTest
{
    @Inject
    DatabaseModel database

    @Before
    void before()
    {
        db.update("DELETE FROM testtableAdmin")
    }

    @Test
    void simple()
    {
        def id = database.testtableAdmin << [
                name : "test",
                valueCol: 1
        ]

        def rec = database.testtableAdmin[id]

        assertEquals "test", rec.$name
        assertEquals 1, rec.$valueCol

    }

    @Test
    void nullInLongColumn()
    {
        def id = database.testtableAdmin << [
                name : "test",
                valueCol: null
        ]

        def rec = database.testtableAdmin[id]

        assertEquals "test", rec.$name
        assertEquals 111, rec.$valueCol
    }

}