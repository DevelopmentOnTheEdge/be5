package com.developmentontheedge.be5.api.services.impl

import com.developmentontheedge.be5.api.services.QRecService
import com.developmentontheedge.be5.api.services.SqlService
import com.developmentontheedge.be5.databasemodel.impl.DatabaseModel
import com.developmentontheedge.be5.test.AbstractProjectIntegrationH2Test
import org.junit.Before
import org.junit.Test

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertNotNull

class QRecServiceImplTest extends AbstractProjectIntegrationH2Test
{
    SqlService db = injector.getSqlService()
    QRecService qRec = injector.get(QRecService.class)
    DatabaseModel database = injector.get(DatabaseModel.class)

    @Before
    void before(){
        database.testtableAdmin.removeAll()
    }

    @Test
    void test()
    {
        String id = database.testtableAdmin << [ "name": "TestName", "value": "1"]

        def rec = qRec.of("SELECT * FROM testtableAdmin WHERE id = ?", id)

        assertNotNull rec
        assertEquals "TestName", rec.getProperty("name").getValue()
        assertEquals "TestName", rec.$name
    }

    @Test
    void testGetters()
    {
        String id = database.testtableAdmin << [ "name": "TestName", "value": 123]

        assertEquals "TestName", qRec.of("SELECT name FROM testtableAdmin WHERE id = ?", id).getString()
        assertEquals 123, qRec.of("SELECT value FROM testtableAdmin WHERE id = ?", id).getInt()
    }

    @Test
    void testNullIfNoRecords()
    {
        assertEquals null, qRec.of("SELECT * FROM testtableAdmin WHERE name = ?", "not contain name")
    }


}