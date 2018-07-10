package com.developmentontheedge.be5.modules.core.operations.system

import com.developmentontheedge.be5.metadata.RoleType
import com.developmentontheedge.be5.modules.core.CoreBe5ProjectDBTest
import com.developmentontheedge.be5.operation.model.OperationStatus
import com.developmentontheedge.be5.test.BaseTestUtils
import com.developmentontheedge.be5.test.mocks.DbServiceMock
import com.developmentontheedge.beans.json.JsonFactory
import org.junit.Before
import org.junit.Test

import static org.junit.Assert.assertEquals
import static org.mockito.Matchers.anyString
import static org.mockito.Matchers.anyVararg
import static org.mockito.Mockito.when


class SessionVariablesEditTest extends CoreBe5ProjectDBTest
{
    @Before
    void setUp()
    {
        initUserWithRoles(RoleType.ROLE_SYSTEM_DEVELOPER)
    }

    @Test
    void testGet()
    {
        session.set("remoteAddr", "199.168.0.1")

        Object first = generateOperation("_system_", "Session variables", "SessionVariablesEdit", "remoteAddr", "").getFirst()

        assertEquals("{" +
                "'values':{" +
                "'label':'Тип: java.lang.String'," +
                "'newValue':'199.168.0.1'}," +
                "'meta':{" +
                "'/label':{'displayName':'label','labelField':true}," +
                "'/newValue':{'displayName':'Новое значение:'}}," +
                "'order':['/label','/newValue']" +
                "}", BaseTestUtils.oneQuotes(JsonFactory.bean(first)))
    }

    @Test
    void testInvoke()
    {
        session.set("remoteAddr", "199.168.0.1")
        //setSession("remoteAddr", "199.168.0.1")

        when(DbServiceMock.mock.one(anyString(), anyVararg())).thenReturn(1L)

        def operation = executeOperation("_system_", "Session variables", "SessionVariablesEdit", "remoteAddr", ["newValue": "199.168.0.2"])
        assertEquals(OperationStatus.REDIRECTED, operation.getSecond().getStatus())

        assertEquals("199.168.0.2", session.get("remoteAddr"))
    }
}
