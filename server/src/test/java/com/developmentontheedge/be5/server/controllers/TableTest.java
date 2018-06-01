package com.developmentontheedge.be5.server.controllers;

import com.developmentontheedge.be5.web.Response;
import com.developmentontheedge.be5.server.RestApiConstants;
import com.developmentontheedge.be5.web.model.jsonapi.JsonApiModel;
import com.developmentontheedge.be5.test.SqlMockOperationTest;
import com.google.common.collect.ImmutableMap;
import org.junit.Test;

import javax.inject.Inject;
import java.util.Date;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;


public class TableTest extends SqlMockOperationTest
{
    @Inject private TableController component;

    @Test
    public void generate()
    {
        Response response = mock(Response.class);

        component.generate(getSpyMockRequest("/api/table/", ImmutableMap.of(
                RestApiConstants.ENTITY,"testtable",
                RestApiConstants.TIMESTAMP_PARAM,"" + new Date().getTime(),
                RestApiConstants.QUERY,"All records")), response);

        verify(response).sendAsJson(any(JsonApiModel.class));
    }

}