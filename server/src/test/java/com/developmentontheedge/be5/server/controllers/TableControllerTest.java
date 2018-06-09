package com.developmentontheedge.be5.server.controllers;

import com.developmentontheedge.be5.server.RestApiConstants;
import com.developmentontheedge.be5.server.helpers.JsonApiResponseHelper;
import com.developmentontheedge.be5.server.model.jsonapi.ErrorModel;
import com.developmentontheedge.be5.server.model.jsonapi.JsonApiModel;
import com.developmentontheedge.be5.test.ServerBe5ProjectTest;
import com.developmentontheedge.be5.test.ServerTestResponse;
import com.developmentontheedge.be5.web.Request;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;

import javax.inject.Inject;
import java.util.Date;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;


public class TableControllerTest extends ServerBe5ProjectTest
{
    @Inject private TableController controller;
    @Inject private JsonApiResponseHelper responseHelper;

    @Before
    public void setUp()
    {
        initGuest();
        ServerTestResponse.newMock();
    }

    @Test
    public void generate()
    {
        controller.generate(getSpyMockRequest("/api/table/", ImmutableMap.of(
                RestApiConstants.ENTITY,"testtable",
                RestApiConstants.QUERY,"All records",
                RestApiConstants.TIMESTAMP_PARAM,"" + new Date().getTime())), ServerTestResponse.mock);

        verify(ServerTestResponse.mock).sendAsJson(any(JsonApiModel.class));
    }

    @Test
    public void accessDenied()
    {
        Request request = getSpyMockRequest("/api/table/", ImmutableMap.of(
                RestApiConstants.ENTITY, "testtableAdmin",
                RestApiConstants.QUERY, "All records",
                RestApiConstants.TIMESTAMP_PARAM, "" + new Date().getTime()));

        controller.generate(request, ServerTestResponse.mock);

        verify(ServerTestResponse.mock).sendAsJson(JsonApiModel.error(
                new ErrorModel("403", "Access denied to query: testtableAdmin.All records"),
                responseHelper.getDefaultMeta(request)));
    }

    @Test
    public void error()
    {
        Request request = getSpyMockRequest("/api/table/", ImmutableMap.of(
                RestApiConstants.ENTITY, "testtable",
                RestApiConstants.QUERY, "Query with error",
                RestApiConstants.TIMESTAMP_PARAM, "" + new Date().getTime()));

        controller.generate(request, ServerTestResponse.mock);

        verify(ServerTestResponse.mock).sendAsJson(JsonApiModel.error(
                new ErrorModel("500", "Internal error occurred during query: testtable.Query with error"),
                responseHelper.getDefaultMeta(request)));
    }
}