package com.developmentontheedge.be5.controllers;

import com.developmentontheedge.be5.server.controllers.QueryBuilderController;
import com.developmentontheedge.be5.web.Response;
import com.developmentontheedge.be5.api.RestApiConstants;
import javax.inject.Inject;
import com.developmentontheedge.be5.web.model.jsonapi.JsonApiModel;
import com.developmentontheedge.be5.server.test.SqlMockOperationTest;
import com.google.common.collect.ImmutableMap;
import org.junit.Test;

import java.util.Date;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;


public class QueryBuilderTest extends SqlMockOperationTest
{
    @Inject private QueryBuilderController component;

    @Test
    public void generate()
    {
        Response response = mock(Response.class);
        component.generate(getSpyMockRequest("queryBuilder", ImmutableMap.of(
                "sql", "select * from testtable limit 1",
                RestApiConstants.TIMESTAMP_PARAM, "" + new Date().getTime())), response);

        verify(response).sendAsJson(any(JsonApiModel.class));
    }

}