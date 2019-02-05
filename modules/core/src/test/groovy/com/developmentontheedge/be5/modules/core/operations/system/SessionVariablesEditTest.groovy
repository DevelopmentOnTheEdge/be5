package com.developmentontheedge.be5.modules.core.operations.system

import com.developmentontheedge.be5.metadata.RoleType
import com.developmentontheedge.be5.modules.core.CoreBe5ProjectDbMockTest
import com.developmentontheedge.be5.operation.OperationStatus
import com.developmentontheedge.beans.json.JsonFactory
import org.junit.Before
import org.junit.Test

import static org.junit.Assert.assertEquals


class SessionVariablesEditTest extends CoreBe5ProjectDbMockTest
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
                "}", oneQuotes(JsonFactory.bean(first)))
    }

    @Test
    void testInvoke()
    {
        session.set("remoteAddr", "199.168.0.1")

        def operation = executeOperation("_system_", "Session variables", "SessionVariablesEdit", "remoteAddr", ["newValue": "199.168.0.2"])
        assertEquals(OperationStatus.FINISHED, operation.getSecond().getStatus())

        assertEquals("199.168.0.2", session.get("remoteAddr"))
    }
}
