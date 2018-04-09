package com.developmentontheedge.be5.env.impl;

import com.developmentontheedge.be5.api.Component;
import com.developmentontheedge.be5.api.Response;
import com.developmentontheedge.be5.test.Be5ProjectTest;
import com.developmentontheedge.be5.test.mocks.SqlServiceMock;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;


public class InjectorTests extends Be5ProjectTest
{
    @Test
    public void componentWithInjectAnnotation()
    {
        Component component = getComponent("testComponentWithInjectAnnotation");

        component.generate(getMockRequest(""), mock(Response.class), null);

        verify(SqlServiceMock.mock).update("select 1");
    }
}
