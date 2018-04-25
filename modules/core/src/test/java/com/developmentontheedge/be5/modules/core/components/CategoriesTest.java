package com.developmentontheedge.be5.modules.core.components;

import com.developmentontheedge.be5.api.Component;
import com.developmentontheedge.be5.api.Response;
import com.developmentontheedge.be5.inject.Inject;
import com.developmentontheedge.be5.inject.Injector;
import com.developmentontheedge.be5.test.Be5ProjectTest;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;


public class CategoriesTest extends Be5ProjectTest
{
    @Inject private Injector injector;
    private static Component component;

    @Before
    public void init()
    {
        component = (Component)injector.getComponent("categories");
    }

    @Test
    public void generate()
    {
        Response response = mock(Response.class);

        component.generate(getMockRequest("forest"), response, injector);

        verify(response).sendAsRawJson(eq(new ArrayList<>()));
    }

    @Test
    public void sendUnknownActionError()
    {
        Response response = mock(Response.class);

        component.generate(getMockRequest(""), response, injector);

        verify(response).sendUnknownActionError();
    }
}