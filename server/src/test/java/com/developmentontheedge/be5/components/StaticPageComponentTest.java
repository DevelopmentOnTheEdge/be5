package com.developmentontheedge.be5.components;

import com.developmentontheedge.be5.api.Request;
import com.developmentontheedge.be5.env.Inject;
import com.developmentontheedge.be5.env.Injector;
import com.developmentontheedge.be5.model.StaticPagePresentation;
import com.developmentontheedge.be5.model.jsonapi.ErrorModel;
import com.developmentontheedge.be5.model.jsonapi.JsonApiModel;
import com.developmentontheedge.be5.model.jsonapi.ResourceData;
import com.developmentontheedge.be5.test.Be5ProjectTest;
import com.developmentontheedge.be5.api.Component;
import com.developmentontheedge.be5.api.Response;
import com.developmentontheedge.be5.api.exceptions.Be5ErrorCode;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.Map;

import static com.developmentontheedge.be5.components.FrontendConstants.STATIC_ACTION;
import static com.developmentontheedge.be5.components.RestApiConstants.SELF_LINK;
import static com.developmentontheedge.be5.components.RestApiConstants.TIMESTAMP_PARAM;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMapOf;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;


public class StaticPageComponentTest extends Be5ProjectTest
{
    @Inject private Injector injector;
    private Component component;

    @Before
    public void init(){
        component = injector.getComponent("static");
    }

    @Test
    public void generate() throws Exception
    {
        Response response = mock(Response.class);
        String page = "info.be";
        Request req = getSpyMockRequest(page, ImmutableMap.of(TIMESTAMP_PARAM, "123456789"));


        component.generate(req, response, injector);

//todo         verify(response).sendAsJson(eq(new ResourceData(STATIC_ACTION,
//                        new StaticPagePresentation("", "<h1>Info</h1><p>Test text.</p>"))),
//                eq(ImmutableMap.of(TIMESTAMP_PARAM, "123456789")),
//                eq(Collections.singletonMap(SELF_LINK, "static/" + page)));
    }

    @Test
    public void generateFoo() throws Exception
    {
        Response response = mock(Response.class);

        String page = "foo.be";
        component.generate(getMockRequest(page), response, injector);

        verify(response).sendErrorAsJson(any(ErrorModel.class), any(Map.class));
    }

}