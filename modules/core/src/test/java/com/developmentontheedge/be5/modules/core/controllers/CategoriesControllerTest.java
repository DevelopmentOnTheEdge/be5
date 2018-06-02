package com.developmentontheedge.be5.modules.core.controllers;

import com.developmentontheedge.be5.modules.core.CoreBe5ProjectDbMockTest;
import com.developmentontheedge.be5.web.Response;
import javax.inject.Inject;
import org.junit.Test;

import java.util.ArrayList;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;


public class CategoriesControllerTest extends CoreBe5ProjectDbMockTest
{
    @Inject private CategoriesController component;

    @Test
    public void generate()
    {
        Response response = mock(Response.class);

        component.generate(getMockRequest("/api/categories/forest"), response);

        verify(response).sendAsRawJson(eq(new ArrayList<>()));
    }

    @Test
    public void sendUnknownActionError()
    {
        Response response = mock(Response.class);

        component.generate(getMockRequest("/api/categories/"), response);

        verify(response).sendUnknownActionError();
    }
}