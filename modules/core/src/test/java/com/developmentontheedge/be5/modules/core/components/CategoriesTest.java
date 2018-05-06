package com.developmentontheedge.be5.modules.core.components;

import com.developmentontheedge.be5.api.Response;
import javax.inject.Inject;
import com.developmentontheedge.be5.test.Be5ProjectTest;
import org.junit.Test;

import java.util.ArrayList;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;


public class CategoriesTest extends Be5ProjectTest
{
    @Inject private Categories component;

    @Test
    public void generate()
    {
        Response response = mock(Response.class);

        component.generate(getMockRequest("forest"), response);

        verify(response).sendAsRawJson(eq(new ArrayList<>()));
    }

    @Test
    public void sendUnknownActionError()
    {
        Response response = mock(Response.class);

        component.generate(getMockRequest(""), response);

        verify(response).sendUnknownActionError();
    }
}