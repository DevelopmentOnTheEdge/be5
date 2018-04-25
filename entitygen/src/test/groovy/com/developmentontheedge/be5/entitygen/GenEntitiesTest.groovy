package com.developmentontheedge.be5.entitygen

import com.developmentontheedge.be5.api.helpers.DpsHelper
import com.developmentontheedge.be5.inject.Inject
import com.developmentontheedge.be5.experimental.entitygen.generate.EntityGenEntityModels
import com.developmentontheedge.be5.test.Be5ProjectTest
import com.developmentontheedge.be5.test.mocks.SqlServiceMock
import com.developmentontheedge.be5.util.DateUtils
import groovy.transform.TypeChecked
import org.junit.Test

import java.sql.Timestamp

import static org.junit.Assert.assertEquals
import static org.mockito.Matchers.*
import static org.mockito.Mockito.verify
import static org.mockito.Mockito.when


@TypeChecked
class GenEntitiesTest extends Be5ProjectTest
{
    @Inject EntityGenEntityModels entities
    @Inject DpsHelper dpsHelper

    private static String getNewValue()
    {
        return "dfsdf"
    }

    @Test
    void insert()
    {
        def dayTime = new Timestamp(DateUtils.curDay().getTime())
        when(SqlServiceMock.mock.insert(anyString(), anyVararg())).thenReturn(123L)

        String id = entities.classifications.insert {
            categoryID = 5L
            recordID   = getNewValue()
            creationDate___ = dayTime
        }

        entities.systemSettings.insert {
            section_name  = "system"
            setting_name  = "test"
            setting_value = "true"
        }

        entities.user_roles.insert {
            user_name = "user"
            role_name = "Tester"
        }

        verify(SqlServiceMock.mock).insert(
                eq("INSERT INTO classifications (recordID, categoryID, creationDate___, whoInserted___) VALUES (?, ?, ?, ?)"),
                eq("dfsdf"),
                eq(5L),
                any(Timestamp),
                eq("Guest")
        )

        assertEquals "123", id
    }

    @Test
    void findOne()
    {
        //entities.provinces.findOne(4)
    }
}
