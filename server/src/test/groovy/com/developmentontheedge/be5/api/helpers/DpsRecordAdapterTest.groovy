package com.developmentontheedge.be5.api.helpers

import com.developmentontheedge.be5.api.services.databasemodel.impl.DatabaseModel
import javax.inject.Inject
import com.developmentontheedge.be5.test.ServerBe5ProjectDBTest
import org.junit.Before;
import org.junit.Test

import static org.junit.Assert.*


class DpsRecordAdapterTest extends ServerBe5ProjectDBTest
{
    @Inject DatabaseModel database

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
                value: 1
        ]

        def rec = database.testtableAdmin[id]

        assertEquals "test", rec.$name
        assertEquals 1, rec.$value

    }

    @Test
    void nullInLongColumn()
    {
        def id = database.testtableAdmin << [
                name : "test",
                value: null
        ]

        def rec = database.testtableAdmin[id]

        assertEquals "test", rec.$name
        assertEquals null, rec.$value
    }

}