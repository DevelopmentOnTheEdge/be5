package com.developmentontheedge.be5.modules.core.controllers;

import com.developmentontheedge.be5.web.Response;
import javax.inject.Inject;
import org.junit.Test;

import java.util.ArrayList;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;


public class CategoriesTest extends CoreBe5ProjectTest
{
    @Inject private CategoriesController component;

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