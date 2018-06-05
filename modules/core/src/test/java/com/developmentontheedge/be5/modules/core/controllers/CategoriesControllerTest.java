package com.developmentontheedge.be5.modules.core.controllers;

import com.developmentontheedge.be5.modules.core.CoreBe5ProjectDbMockTest;
import com.developmentontheedge.be5.web.Response;
import org.junit.Test;

import javax.inject.Inject;
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

        verify(response).sendAsJson(eq(new ArrayList<>()));
    }

    @Test
    public void sendUnknownActionError()
    {
        Response response = mock(Response.class);

        component.generate(getMockRequest("/api/categories/"), response);

        verify(response).sendErrorAsJson(eq("Unknown action"), eq(404));
    }
}