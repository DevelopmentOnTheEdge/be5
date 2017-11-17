package com.developmentontheedge.be5.api.components.impl.model

import com.developmentontheedge.be5.api.Component
import com.developmentontheedge.be5.api.Response
import com.developmentontheedge.be5.components.RestApiConstants
import com.developmentontheedge.be5.env.Inject
import com.developmentontheedge.be5.env.Injector
import com.developmentontheedge.be5.metadata.RoleType
import com.developmentontheedge.be5.model.jsonapi.ResourceData
import com.developmentontheedge.be5.test.Be5ProjectTest
import com.google.common.collect.ImmutableMap
import com.google.gson.Gson
import org.junit.Before
import org.junit.Test

import static org.mockito.Matchers.any
import static org.mockito.Matchers.anyMapOf
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.verify

class FormTest extends Be5ProjectTest
{
    @Inject private Injector injector
    private Component component

    @Before
    void init()
    {
        component = injector.getComponent("form")
    }

    @Test
    void generate() throws Exception {
        initUserWithRoles(RoleType.ROLE_ADMINISTRATOR, RoleType.ROLE_SYSTEM_DEVELOPER)

        Response response = mock(Response.class)
        String values = new Gson().toJson(ImmutableMap.of(
                "name","test1",
                "value", "2"))

        component.generate(getSpyMockRequest("", ImmutableMap.<String, String>builder()
                .put(RestApiConstants.ENTITY, "testtableAdmin")
                .put(RestApiConstants.QUERY, "All records")
                .put(RestApiConstants.OPERATION, "Insert")
                .put(RestApiConstants.SELECTED_ROWS, "0")
                .put(RestApiConstants.TIMESTAMP_PARAM, "" + new Date().getTime())
                .put(RestApiConstants.VALUES, values).build()), response, injector)

        verify(response).sendAsJson(any(ResourceData.class), any(Map.class), anyMapOf(String.class, String.class))

        initUserWithRoles(RoleType.ROLE_GUEST)
    }

}