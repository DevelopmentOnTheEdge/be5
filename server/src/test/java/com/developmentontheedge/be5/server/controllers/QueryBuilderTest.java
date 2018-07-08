package com.developmentontheedge.be5.server.controllers;

import com.developmentontheedge.be5.server.RestApiConstants;
import com.developmentontheedge.be5.server.model.jsonapi.JsonApiModel;
import com.developmentontheedge.be5.test.SqlMockOperationTest;
import com.developmentontheedge.be5.web.Response;
import com.google.common.collect.ImmutableMap;
import org.junit.Test;

import javax.inject.Inject;
import java.util.Date;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;


public class QueryBuilderTest extends SqlMockOperationTest
{
    @Inject
    private QueryBuilderController component;

    @Test
    public void generate()
    {
        Response response = mock(Response.class);
        component.generate(getSpyMockRequest("/api/queryBuilder/", ImmutableMap.of(
                "sql", "select * from testtable limit 1",
                RestApiConstants.TIMESTAMP_PARAM, "" + new Date().getTime())), response);

        verify(response).sendAsJson(any(JsonApiModel.class));
    }

}