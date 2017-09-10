package com.developmentontheedge.be5.modules.core.genegate

import com.developmentontheedge.be5.env.Inject
import com.developmentontheedge.be5.test.Be5ProjectTest
import com.developmentontheedge.be5.test.mocks.SqlServiceMock
import com.developmentontheedge.be5.util.DateUtils
import com.developmentontheedge.beans.DynamicPropertySet
import com.developmentontheedge.beans.DynamicPropertySetSupport
import groovy.transform.TypeChecked
import org.junit.Test

import static org.junit.Assert.*
import static org.mockito.Mockito.*

import static com.developmentontheedge.be5.model.beans.DynamicPropertyGBuilder.*
//import static com.developmentontheedge.be5.modules.core.genegate.CoreEntityFields.UsersFields.*
//

class EntitiesTest extends Be5ProjectTest
{
    @Inject CoreEntityModels entities

    @Test
    void name()
    {
        def day = DateUtils.curDay()
        when(SqlServiceMock.mock.insert(anyString(), anyVararg())).thenReturn(123L)

//        DynamicPropertySet.metaClass.$ = { String name -> delegate.getValue(name) }
        DynamicPropertySet dps = new DynamicPropertySetSupport()

        add(dps) { name = "user_name"; value = "test" }
        add(dps) { name = "registrationDate"; TYPE = Date; value = day }

        add(dps) {
            name        = "attempt"
            TYPE        = Integer
            value       = 345
            CAN_BE_NULL = false
        }

        String id = entities.users.insert{
            user_name        = dps.$user_name
            registrationDate = dps.$registrationDate
            attempt          = dps.$attempt
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
