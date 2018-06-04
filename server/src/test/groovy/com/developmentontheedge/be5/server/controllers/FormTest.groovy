package com.developmentontheedge.be5.server.controllers

import com.developmentontheedge.be5.metadata.RoleType
import com.developmentontheedge.be5.server.RestApiConstants
import com.developmentontheedge.be5.test.ServerBe5ProjectTest
import com.developmentontheedge.be5.web.Response
import com.google.common.collect.ImmutableMap
import groovy.transform.TypeChecked
import org.junit.After
import org.junit.Before
import org.junit.Test

import javax.inject.Inject

import static org.mockito.Mockito.mock

@TypeChecked
class FormTest extends ServerBe5ProjectTest
{
    @Inject private FormController component
    private Response response

    @Before
    void setUp()
    {
        response = mock(Response.class)
    }

    @After
    void tearDown()
    {
        initUserWithRoles(RoleType.ROLE_GUEST)
    }

    @Test
    void generate()
    {
        initUserWithRoles(RoleType.ROLE_ADMINISTRATOR)

        generateForQuery("All records")

        //TODO verify(response).sendAsJson(any(ResourceData.class), any(Map.class))
    }

    @Test
    void operationNotAvailableForThisQuery()
    {
        initUserWithRoles(RoleType.ROLE_ADMINISTRATOR)

        generateForQuery("Query without operations")

        //TODO verify(response).sendErrorAsJson(any(ErrorModel.class), any(Map.class))
    }

    @Test
    void operationRequiredRoleNotFound()
    {
        initUserWithRoles(RoleType.ROLE_GUEST)

        generateForQuery("Query without operations")

        //TODO verify(response).sendErrorAsJson(any(ErrorModel.class), any(Map.class))
    }

    def generateForQuery(String queryName)
    {
        String values = jsonb.toJson([name:"test1", value: "2"])

        component.generate(getSpyMockRequest("/api/form/", ImmutableMap.<String, Object>builder()
                .put(RestApiConstants.ENTITY, "testtableAdmin")
                .put(RestApiConstants.QUERY, queryName)
                .put(RestApiConstants.OPERATION, "Insert")
                .put(RestApiConstants.SELECTED_ROWS, "")
                .put(RestApiConstants.OPERATION_PARAMS, jsonb.toJson([name:"test1"]))
                .put(RestApiConstants.TIMESTAMP_PARAM, "" + new Date().getTime())
                .put(RestApiConstants.VALUES, values).build()), response)
    }

}