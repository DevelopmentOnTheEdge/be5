package com.developmentontheedge.be5.modules.core.genegate

import com.developmentontheedge.be5.env.Inject
import com.developmentontheedge.be5.test.Be5ProjectTest
import com.developmentontheedge.be5.test.mocks.SqlServiceMock
import com.developmentontheedge.be5.util.DateUtils
import com.developmentontheedge.beans.DynamicPropertySetSupport
import groovy.transform.TypeChecked
import org.junit.Test

import static org.junit.Assert.*
import static org.mockito.Mockito.*

@TypeChecked
class EntitiesTest extends Be5ProjectTest
{
    @Inject CoreEntityModels entities

    @Test
    void name()
    {
        def day = DateUtils.curDay()
        when(SqlServiceMock.mock.insert(anyString(), anyVararg())).thenReturn(123L)

        String id = entities.users.insert{
            user_name        = "test"
            registrationDate = day
            attempt          = 345
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
