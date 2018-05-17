package com.developmentontheedge.be5.controllers;

import com.developmentontheedge.be5.api.Request;
import javax.inject.Inject;

import com.developmentontheedge.be5.model.jsonapi.ErrorModel;
import com.developmentontheedge.be5.model.jsonapi.ResourceData;
import com.developmentontheedge.be5.test.ServerBe5ProjectTest;
import com.developmentontheedge.be5.api.Response;
import com.google.common.collect.ImmutableMap;
import org.junit.Test;

import java.util.Map;

import static com.developmentontheedge.be5.api.RestApiConstants.TIMESTAMP_PARAM;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;


public class StaticPageControllerTest extends ServerBe5ProjectTest
{
    @Inject private StaticPageController component;

    @Test
    public void generate()
    {
        Response response = mock(Response.class);
        String page = "info.be";
        Request req = getSpyMockRequest(page, ImmutableMap.of(TIMESTAMP_PARAM, "123456789"));


        component.generate(req, response);

//todo         verify(response).sendAsJson(eq(new ResourceData(STATIC_ACTION,
//                        new StaticPagePresentation("", "<h1>Info</h1><p>Test text.</p>"))),
//                eq(ImmutableMap.of(TIMESTAMP_PARAM, "123456789")),
//                eq(Collections.singletonMap(SELF_LINK, "static/" + page)));

        verify(response).sendAsJson(any(ResourceData.class), any(Map.class));
    }

    @Test
    public void generateFoo()
    {
        Response response = mock(Response.class);

        String page = "foo.be";
        component.generate(getMockRequest(page), response);

        verify(response).sendErrorAsJson(any(ErrorModel.class), any(Map.class));
    }

}