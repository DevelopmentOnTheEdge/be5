package com.developmentontheedge.be5.server.controllers;

import com.developmentontheedge.be5.metadata.RoleType;
import com.developmentontheedge.be5.server.RestApiConstants;
import com.developmentontheedge.be5.server.model.jsonapi.ErrorModel;
import com.developmentontheedge.be5.server.model.jsonapi.JsonApiModel;
import com.developmentontheedge.be5.test.ServerBe5ProjectTest;
import com.developmentontheedge.be5.test.ServerTestResponse;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;

import javax.inject.Inject;
import java.util.Collections;
import java.util.Date;

import static junit.framework.TestCase.assertNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;


public class TableControllerTest extends ServerBe5ProjectTest
{
    @Inject private TableController tableController;

    @Before
    public void setUp()
    {
        initGuest();
        ServerTestResponse.newMock();
    }

    @Test
    public void generate()
    {
        tableController.generate(getSpyMockRequest("/api/table/", ImmutableMap.of(
                RestApiConstants.ENTITY,"testtable",
                RestApiConstants.QUERY,"All records",
                RestApiConstants.TIMESTAMP_PARAM,"" + new Date().getTime())), ServerTestResponse.mock);

        verify(ServerTestResponse.mock).sendAsJson(any(JsonApiModel.class));
    }

    @Test
    public void getQueryJsonApiForUser()
    {
        JsonApiModel queryJsonApiForUser = tableController.
                getQueryJsonApiForUser("testtable", "All records", Collections.emptyMap());

        assertNotNull(queryJsonApiForUser.getData());
        assertNull(queryJsonApiForUser.getErrors());
    }

    @Test
    public void accessDenied()
    {
        JsonApiModel queryJsonApiForUser = tableController.
                getQueryJsonApiForUser("testtableAdmin", "All records", Collections.emptyMap());

        assertEquals(new ErrorModel("403", "Access denied to query: testtableAdmin.All records",
                            Collections.singletonMap("self", "table/testtableAdmin/All records")),
                queryJsonApiForUser.getErrors()[0]);
    }

    @Test
    public void accessAllowed()
    {
        initUserWithRoles(RoleType.ROLE_SYSTEM_DEVELOPER);

        JsonApiModel queryJsonApiForUser = tableController.
                getQueryJsonApiForUser("testtableAdmin", "All records", Collections.emptyMap());

        assertNotNull(queryJsonApiForUser.getData());
        assertNull(queryJsonApiForUser.getErrors());
    }

    @Test
    public void error()
    {
        JsonApiModel queryJsonApiForUser = tableController.getQueryJsonApiForUser("testtable", "Query with error", Collections.emptyMap());

        assertEquals(null, queryJsonApiForUser.getData());
        assertEquals(new ErrorModel("500", "Internal error occurred during query: testtable.Query with error",
                            Collections.singletonMap("self", "table/testtable/Query with error")),
                queryJsonApiForUser.getErrors()[0]);
    }
}