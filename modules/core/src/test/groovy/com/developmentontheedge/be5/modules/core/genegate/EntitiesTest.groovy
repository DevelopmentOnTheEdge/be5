package com.developmentontheedge.be5.modules.core.genegate

import com.developmentontheedge.be5.api.helpers.DpsHelper
import com.developmentontheedge.be5.env.Inject
import com.developmentontheedge.be5.modules.core.genegate.fields.ProvincesFields as p
import com.developmentontheedge.be5.test.Be5ProjectTest
import com.developmentontheedge.be5.test.mocks.SqlServiceMock
import com.developmentontheedge.be5.util.DateUtils
import com.developmentontheedge.beans.DynamicPropertySet
import org.junit.Ignore
import org.junit.Test
import java.sql.Date

import static org.junit.Assert.*
import static org.mockito.Mockito.*


class EntitiesTest extends Be5ProjectTest
{
    @Inject CoreEntityModels entities
    @Inject DpsHelper dpsHelper

    String getString(Object a)
    {
        return a
    }

    @Ignore("No signature of method getString()")
    @Test
    void name()
    {
        def day = DateUtils.curDay()
        when(SqlServiceMock.mock.insert(anyString(), anyVararg())).thenReturn(123L)

        DynamicPropertySet dps = dpsHelper.getDpsForColumns(entities.provinces.getEntity(), [p.name, p.countryID])

        add(dps) {
            name        = p.ID
            TYPE        = String
            value       = "12"
            CAN_BE_NULL = false
        }

        edit(dps, p.name) { value = "testName" }
        edit(dps, p.countryID) { value = "testCountryID" }

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
}
