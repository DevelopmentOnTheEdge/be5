package com.developmentontheedge.be5.server.controllers;

import com.developmentontheedge.be5.metadata.RoleType;
import com.developmentontheedge.be5.server.model.TablePresentation;
import com.developmentontheedge.be5.server.model.jsonapi.ErrorModel;
import com.developmentontheedge.be5.server.model.jsonapi.JsonApiModel;
import com.developmentontheedge.be5.test.ServerBe5ProjectTest;
import com.developmentontheedge.be5.test.ServerTestResponse;
import com.google.common.collect.ImmutableMap;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;

import javax.inject.Inject;
import java.util.Collections;

import static com.developmentontheedge.be5.server.RestApiConstants.ENTITY;
import static com.developmentontheedge.be5.server.RestApiConstants.QUERY;
import static com.developmentontheedge.be5.server.RestApiConstants.TIMESTAMP_PARAM;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;


public class DocumentControllerTest extends ServerBe5ProjectTest
{
    @Inject
    private DocumentController documentController;

    @Before
    public void setUp()
    {
        initGuest();
        ServerTestResponse.newMock();
    }

    @Test
    public void generate()
    {
        JsonApiModel jsonApiModel = documentController.generate(getSpyMockRequest("/api/table/", ImmutableMap.of(
                ENTITY, "testtable",
                QUERY, "All records",
                TIMESTAMP_PARAM, "" + System.currentTimeMillis())), "");

        assertEquals("testtable: All records", ((TablePresentation) jsonApiModel.getData().getAttributes()).getTitle());
    }

    @Test
    public void accessDenied()
    {
        JsonApiModel jsonApiModel = documentController.generate(getSpyMockRequest("/api/table/", ImmutableMap.of(
                ENTITY, "testtableAdmin",
                QUERY, "All records",
                TIMESTAMP_PARAM, "" + System.currentTimeMillis())), "");

        assertEquals(new ErrorModel("403",
                        "Access denied to query: testtableAdmin.All records",
                        Collections.singletonMap("self", "table/testtableAdmin/All records")),
                jsonApiModel.getErrors()[0]);
    }

    @Test
    public void accessAllowed()
    {
        initUserWithRoles(RoleType.ROLE_SYSTEM_DEVELOPER);

        JsonApiModel jsonApiModel = documentController.generate(getSpyMockRequest("/api/table/", ImmutableMap.of(
                ENTITY, "testtableAdmin", QUERY, "All records",
                TIMESTAMP_PARAM, "" + System.currentTimeMillis())), "");

        assertNotNull(jsonApiModel.getData());
        TestCase.assertNull(jsonApiModel.getErrors());
    }

    @Test
    public void error()
    {
        JsonApiModel jsonApiModel = documentController.generate(getSpyMockRequest("/api/table/", ImmutableMap.of(
                ENTITY, "testtable", QUERY, "Query with error",
                TIMESTAMP_PARAM, "" + System.currentTimeMillis())), "");

        assertNull(jsonApiModel.getData());
        assertEquals(new ErrorModel("500",
                        "Internal error occurred during query: testtable.Query with error",
                        Collections.singletonMap("self", "table/testtable/Query with error")),
                jsonApiModel.getErrors()[0]);
    }
}
