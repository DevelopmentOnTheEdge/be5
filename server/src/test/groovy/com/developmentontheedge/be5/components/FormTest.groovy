package com.developmentontheedge.be5.components

import com.developmentontheedge.be5.api.Component
import com.developmentontheedge.be5.api.Response
import com.developmentontheedge.be5.api.RestApiConstants
import com.developmentontheedge.be5.env.Inject
import com.developmentontheedge.be5.env.Injector
import com.developmentontheedge.be5.metadata.RoleType
import com.developmentontheedge.be5.model.jsonapi.ResourceData
import com.developmentontheedge.be5.test.Be5ProjectTest
import com.google.common.collect.ImmutableMap
import groovy.transform.TypeChecked
import org.junit.Before
import org.junit.Test

import static org.mockito.Matchers.any
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.verify


@TypeChecked
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
    void generate()
    {
        initUserWithRoles(RoleType.ROLE_ADMINISTRATOR, RoleType.ROLE_SYSTEM_DEVELOPER)

        Response response = mock(Response.class)
        String values = jsonb.toJson([name:"test1", value: "2"])

        component.generate(getSpyMockRequest("", ImmutableMap.<String, String>builder()
                .put(RestApiConstants.ENTITY, "testtableAdmin")
                .put(RestApiConstants.QUERY, "All records")
                .put(RestApiConstants.OPERATION, "Insert")
                .put(RestApiConstants.SELECTED_ROWS, "")
                .put(RestApiConstants.OPERATION_PARAMS, jsonb.toJson([name:"test1"]))
                .put(RestApiConstants.TIMESTAMP_PARAM, "" + new Date().getTime())
                .put(RestApiConstants.VALUES, values).build()), response, injector)

        verify(response).sendAsJson(any(ResourceData.class), any(Map.class))

        initUserWithRoles(RoleType.ROLE_GUEST)
    }

}