package com.developmentontheedge.be5.server.controllers;

import com.developmentontheedge.be5.metadata.RoleType;
import com.developmentontheedge.be5.server.model.jsonapi.JsonApiModel;
import com.developmentontheedge.be5.test.ServerBe5ProjectTest;
import com.developmentontheedge.be5.test.ServerTestResponse;
import com.developmentontheedge.be5.web.Request;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;

import javax.inject.Inject;

import static com.developmentontheedge.be5.server.RestApiConstants.TIMESTAMP_PARAM;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;


public class StaticPageControllerTest extends ServerBe5ProjectTest
{
    @Inject
    private StaticPageController component;

    @Before
    public void setUp()
    {
        initUserWithRoles(RoleType.ROLE_ADMINISTRATOR, RoleType.ROLE_SYSTEM_DEVELOPER);
        ServerTestResponse.newMock();
    }

    @Test
    public void generate()
    {
        String page = "/api/static/info.be";
        Request req = getSpyMockRequest(page, ImmutableMap.of(TIMESTAMP_PARAM, "123456789"));


        component.generate(req, ServerTestResponse.mock);

//todo         verify(response).sendAsJson(eq(new ResourceData(STATIC_ACTION,
//                        new StaticPagePresentation("", "<h1>Info</h1><p>Test text.</p>"))),
//                eq(ImmutableMap.of(TIMESTAMP_PARAM, "123456789")),
//                eq(Collections.singletonMap(SELF_LINK, "static/" + page)));

        verify(ServerTestResponse.mock).sendAsJson(any(JsonApiModel.class));
    }

    @Test
    public void generateFoo()
    {
        String page = "/api/static/foo.be";
        component.generate(getMockRequest(page), ServerTestResponse.mock);

        verify(ServerTestResponse.mock).sendAsJson(any(JsonApiModel.class), eq(404));
    }
}
