package com.developmentontheedge.be5.server.controllers;

import com.developmentontheedge.be5.metadata.RoleType;
import com.developmentontheedge.be5.operation.OperationConstants;
import com.developmentontheedge.be5.server.RestApiConstants;
import com.developmentontheedge.be5.server.model.FormPresentation;
import com.developmentontheedge.be5.server.model.FormRequest;
import com.developmentontheedge.be5.server.model.jsonapi.JsonApiModel;
import com.developmentontheedge.be5.test.ServerBe5ProjectTest;
import com.developmentontheedge.be5.web.Response;
import com.google.common.collect.ImmutableMap;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.inject.Inject;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static java.util.Collections.emptyMap;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;


public class FormControllerTest extends ServerBe5ProjectTest
{
    @Inject
    private FormController component;

    private Response res;

    @Before
    public void setUp() throws Exception
    {
        res = mock(Response.class);
    }

    @After
    public void tearDown()
    {
        initUserWithRoles(RoleType.ROLE_GUEST);
    }

    @Test
    public void generate()
    {
        initUserWithRoles(RoleType.ROLE_ADMINISTRATOR);

        JsonApiModel jsonApiModel = generateForQuery("All records");

        assertNull(jsonApiModel.getErrors());
        assertNotNull(jsonApiModel.getData());

        assertEquals("Insert", ((FormPresentation) jsonApiModel.getData().getAttributes()).getOperation());
        assertEquals("All records", ((FormPresentation) jsonApiModel.getData().getAttributes()).getQuery());
    }

    @Test
    public void operationNotAvailableForThisQuery()
    {
        initUserWithRoles(RoleType.ROLE_ADMINISTRATOR);

        JsonApiModel jsonApiModel = generateForQuery("Query without operations");

        assertEquals("403", jsonApiModel.getErrors()[0].getStatus());
        assertEquals("Operation 'testtableAdmin.Insert' not assigned to query: 'Query without operations'",
                jsonApiModel.getErrors()[0].getTitle());
    }

    @Test
    public void operationRequiredRoleNotFound()
    {
        initUserWithRoles(RoleType.ROLE_GUEST);

        JsonApiModel jsonApiModel = generateForQuery("All records");

        assertNotNull(jsonApiModel.getErrors());
        assertNull(jsonApiModel.getData());

        assertEquals("403", jsonApiModel.getErrors()[0].getStatus());
        assertEquals("Access denied to operation: testtableAdmin.Insert",
                jsonApiModel.getErrors()[0].getTitle());
    }

    @Test
    public void errorOnExecute()
    {
        initUserWithRoles(RoleType.ROLE_ADMINISTRATOR);

        JsonApiModel jsonApiModel = generateForQuery("All records", "/api/form/apply", emptyMap());

        assertNull(jsonApiModel.getErrors());
        assertNotNull(jsonApiModel.getData());

        verify(res).setStatus(500);
    }

    private JsonApiModel generateForQuery(String queryName)
    {
        Map<String, String> values = new LinkedHashMap<>(2);
        values.put("name", "test1");
        values.put("value", "2");

        return generateForQuery(queryName, "/api/form/", values);
    }

    private JsonApiModel generateForQuery(String queryName, String url, Map<String, String> values)
    {
        FormRequest formRequest = new FormRequest("testtableAdmin", queryName, "Insert", emptyMap());
        String operationParams = jsonb.toJson(formRequest);
        return component.generateJson(getSpyMockRequest(url, ImmutableMap.<String, Object>builder()
                .put(RestApiConstants.OPERATION_PARAMS, operationParams)
                .put(RestApiConstants.TIMESTAMP_PARAM, "" + System.currentTimeMillis())
                .build()), res, url.replace("/api/form/", ""));
    }

}
