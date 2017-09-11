package com.developmentontheedge.be5.modules.core.genegate

import com.developmentontheedge.be5.api.helpers.DpsHelper
import com.developmentontheedge.be5.env.Inject
import com.developmentontheedge.be5.modules.core.genegate.fields.UsersFields as u
import com.developmentontheedge.be5.test.Be5ProjectTest
import com.developmentontheedge.be5.test.mocks.SqlServiceMock
import com.developmentontheedge.be5.util.DateUtils
import com.developmentontheedge.beans.DynamicPropertySet
import groovy.transform.TypeChecked
import org.junit.Test
import java.sql.Date

import static org.junit.Assert.*
import static org.mockito.Mockito.*

import static com.developmentontheedge.be5.model.beans.DynamicPropertyGBuilder.*

@TypeChecked
class EntitiesTest extends Be5ProjectTest
{
    @Inject CoreEntityModels entities
    @Inject DpsHelper dpsHelper

    @Test
    void name()
    {
        def day = DateUtils.curDay()
        when(SqlServiceMock.mock.insert(anyString(), anyVararg())).thenReturn(123L)

        DynamicPropertySet dps = dpsHelper.getDpsForColumns(entities.users.getEntity(), [u.user_name, u.user_pass,
                                         u.emailAddress, u.registrationDate])

        add(dps) {
            name        = u.attempt
            TYPE        = Integer
            value       = 345
            CAN_BE_NULL = false
        }

        edit(dps, u.user_name) { value = "test"}
        edit(dps, u.registrationDate) { value = day}


        String id = entities.users.insert{
            user_name        = dps.getValue(u.user_name)
            registrationDate = (Date)dps.getValue(u.registrationDate)
            attempt          = (Integer)dps.getValue(u.attempt)
        }

        verify(SqlServiceMock.mock).insert(
                eq("INSERT INTO users (user_name, registrationDate, attempt) VALUES (?, ?, ?)"),
                eq("test"),
                refEq(day),
                eq(345)
        )

        assertEquals "123", id
    }
}
