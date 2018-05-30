package com.developmentontheedge.be5.databasemodel.groovy

import com.developmentontheedge.be5.databasemodel.DatabaseModel
import com.developmentontheedge.be5.databasemodel.DatabaseModelProjectDbTest
import com.developmentontheedge.be5.databasemodel.EntityModel
import com.developmentontheedge.be5.databasemodel.RecordModel
import org.junit.Before
import org.junit.Test

import javax.inject.Inject

import static org.junit.Assert.*


class RecordModelTest extends DatabaseModelProjectDbTest
{
    @Inject private DatabaseModel database
    private EntityModel<Long> testtableAdmin

    @Before
    void before()
    {
        testtableAdmin = database.getEntity("testtableAdmin")
        db.update("DELETE FROM testtableAdmin")
    }

    @Test
    void testUpdate()
    {
        Long id = testtableAdmin.add([
                "name": "TestName",
                "value": 1
        ])

        testtableAdmin.set(id, [
                "name": "TestName2"
        ])

        RecordModel<Long> record = testtableAdmin.get(id)

        Long id2 = record.getPrimaryKey()
        assertEquals(Long.class, id2.getClass())

        assertEquals( "TestName2", record.getValue("name"))
        assertEquals( "TestName2", testtableAdmin.get(id).getValue("name"))

        record.update( [ "name": "TestName3" ])

        assertEquals( "TestName3", record.getValue("name"))

        record.update("name", "TestName4")

        assertEquals( "TestName4", record.getValue("name"))

        record.name = "TestName5"

        assertEquals( "TestName5", record.getValue("name"))
    }

    @Test(expected = IllegalAccessError.class)
    void errorOnSetValue()
    {
        Long id = testtableAdmin.add([ "name": "TestName", "value": 1 ])
        RecordModel<Long> record = testtableAdmin.get(id)

        record.setValue("name", "123")
    }

    @Test(expected = IllegalArgumentException.class)
    void propertyNotFound()
    {
        Long id = testtableAdmin.add([ "name": "TestName", "value": 1 ])
        RecordModel<Long> record = testtableAdmin.get(id)

        record.foo = "123"
    }

}