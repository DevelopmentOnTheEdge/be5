package com.developmentontheedge.be5.server.controllers;

import com.developmentontheedge.be5.metadata.RoleType;
import com.developmentontheedge.be5.operation.OperationConstants;
import com.developmentontheedge.be5.server.RestApiConstants;
import com.developmentontheedge.be5.server.model.FormPresentation;
import com.developmentontheedge.be5.server.model.jsonapi.JsonApiModel;
import com.developmentontheedge.be5.test.ServerBe5ProjectTest;
import com.google.common.collect.ImmutableMap;
import org.junit.After;
import org.junit.Test;

import javax.inject.Inject;
import java.util.LinkedHashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;


public class FormTest extends ServerBe5ProjectTest
{
    @Inject
    private FormController component;

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

    private JsonApiModel generateForQuery(String queryName)
    {
        LinkedHashMap<String, String> map = new LinkedHashMap<String, String>(2);
        map.put("name", "test1");
        map.put("value", "2");
        String values = jsonb.toJson(map);

        LinkedHashMap<String, String> map1 = new LinkedHashMap<String, String>(1);
        map1.put("name", "test1");

        return component.generate(getSpyMockRequest("/api/form/", ImmutableMap.<String, Object>builder()
                .put(RestApiConstants.ENTITY, "testtableAdmin")
                .put(RestApiConstants.QUERY, queryName)
                .put(RestApiConstants.OPERATION, "Insert")
                .put(OperationConstants.SELECTED_ROWS, "")
                .put(RestApiConstants.OPERATION_PARAMS, jsonb.toJson(map1))
                .put(RestApiConstants.TIMESTAMP_PARAM, "" + System.currentTimeMillis())
                .put(RestApiConstants.VALUES, values)
                .build()), "");
    }

}
