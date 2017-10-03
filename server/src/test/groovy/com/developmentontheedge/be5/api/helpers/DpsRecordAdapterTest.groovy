package com.developmentontheedge.be5.api.helpers

import com.developmentontheedge.be5.databasemodel.impl.DatabaseModel
import com.developmentontheedge.be5.env.Inject
import com.developmentontheedge.be5.test.Be5ProjectDBTest
import org.junit.Before;
import org.junit.Test

import static org.junit.Assert.*


class DpsRecordAdapterTest extends Be5ProjectDBTest
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