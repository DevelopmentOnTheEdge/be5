package com.developmentontheedge.be5.modules.core.operations

import com.developmentontheedge.be5.api.Request
import com.developmentontheedge.be5.api.Session
import com.developmentontheedge.be5.model.FormPresentation
import com.developmentontheedge.be5.test.SqlMockOperationTest
import com.developmentontheedge.be5.test.mocks.SqlServiceMock
import org.junit.Test

import static org.junit.Assert.assertEquals
import static org.mockito.Matchers.anyString
import static org.mockito.Matchers.anyVararg
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.verify
import static org.mockito.Mockito.when

class SessionVariablesEditTest extends SqlMockOperationTest
{
    @Test
    void testGet()
    {
        Request req = getSpyMockRecForOp("_system_", "Session variables", "SessionVariablesEdit", "remoteAddr",
                "", ["remoteAddr":"199.168.0.1"])

        FormPresentation first = operationService.generate(req).getFirst()

        assertEquals("{" +
                "'values':{" +
                    "'label':'Тип: java.lang.String'," +
                    "'newValue':'199.168.0.1'}," +
                "'meta':{" +
                    "'/label':{'displayName':'label','labelField':true}," +
                    "'/newValue':{'displayName':'Новое значение:'}}," +
                "'order':['/label','/newValue']" +
            "}", oneQuotes(first.getBean().toString()))
    }

    @Test
    void testInvoke()
    {
        Request req = getSpyMockRecForOp("_system_", "Session variables", "SessionVariablesEdit", "remoteAddr",
                "{'newValue':'199.168.0.2'}", ["remoteAddr":"199.168.0.1"])

//        def session = mock(Session)
//        when(req.getSession()).thenReturn(session)
//        when(SqlServiceMock.mock.getScalar(anyString(), anyVararg())).thenReturn(1L)
//
//        operationService.execute(req)
//
//        verify(session).set("remoteAddr", "199.168.0.2")
    }
}
