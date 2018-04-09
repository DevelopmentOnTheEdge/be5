package com.developmentontheedge.be5.entitygen.experimental.gen

import com.developmentontheedge.be5.api.helpers.DpsHelper
import com.developmentontheedge.be5.entitygen.experimental.genegate.CoreEntityModels
import com.developmentontheedge.be5.entitygen.experimental.genegate.entities.Provinces
import com.developmentontheedge.be5.env.Inject
import com.developmentontheedge.be5.model.beans.GDynamicPropertySetSupport
import com.developmentontheedge.be5.test.Be5ProjectTest
import com.developmentontheedge.be5.test.mocks.SqlServiceMock
import org.apache.commons.dbutils.ResultSetHandler
import org.junit.Ignore
import org.junit.Test
import org.mockito.Matchers

import static org.junit.Assert.*
import static org.mockito.Mockito.*


class EntitiesTest extends Be5ProjectTest//todo use Be5ProjectDBTest
{
    @Inject CoreEntityModels entities
    @Inject DpsHelper dpsHelper

    String getString(Object a)
    {
        return a
    }

    @Test
    void addAndDps()
    {
        when(SqlServiceMock.mock.insert(anyString(), anyVararg())).thenReturn(123L)

        GDynamicPropertySetSupport dps = dpsHelper.addDpForColumns(new GDynamicPropertySetSupport(),
                entities.provinces.entity, ["name", "countryID"], [:])

        dps.add("ID") {
            TYPE        = String
            value       = "12"
            CAN_BE_NULL = false
        }

        dps.edit("name") { value = "testName" }
        dps.edit("countryID") { value = "testCountryID" }

        //todo getString(dps.$countryID)
        String id = entities.provinces.add {
            ID          = dps.$ID
            name        = dps.$name
            countryID   = dps.$countryID
        }

        verify(SqlServiceMock.mock).insert(
                eq("INSERT INTO provinces (name, ID, countryID) VALUES (?, ?, ?)"),
                eq("testName"),
                eq("12"),
                eq("testCountryID"),
        )

        assertEquals "123", id
    }

    @Test
    void findOneTest() throws Exception
    {
        def pojo = new Provinces()
        pojo.with {ID = "12"; name = "testName"; countryID = "testCountryID"}

        when(SqlServiceMock.mock.query(anyString(),
                Matchers.<ResultSetHandler<Provinces>> any(), anyVararg())).thenReturn(pojo)

        def province = entities.provinces.findOne("12")

        assertEquals("12",            province.ID)
        assertEquals("testName",      province.name)
        assertEquals("testCountryID", province.countryID)
    }

    @Test
    @Ignore
    void test() throws Exception
    {
        entities.provinces.count()
        entities.provinces.count {countryID = "foo"}

        def firstFooBar = entities.provinces.findFirst { countryID = "foo"; name = "bar" }
        def one =         entities.provinces.findOne("12")
        def all =         entities.provinces.findAll()
        def allFooBar =   entities.provinces.findAll { countryID = "foo"; name = "bar" }


        entities.provinces.removeAll()
        entities.provinces.removeAll {countryID = "foo"}
        entities.provinces.remove("12")

        entities.provinces.exists("12")
    }

}
