package com.developmentontheedge.be5.components;

import com.developmentontheedge.be5.api.Component;
import com.developmentontheedge.be5.api.Response;
import com.developmentontheedge.be5.api.RestApiConstants;
import com.developmentontheedge.be5.env.Inject;
import com.developmentontheedge.be5.env.Injector;
import com.developmentontheedge.be5.model.jsonapi.JsonApiModel;
import com.developmentontheedge.be5.test.SqlMockOperationTest;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;


public class QueryBuilderTest extends SqlMockOperationTest
{
    @Inject private Injector injector;
    private Component component;

    @Before
    public void init()
    {
        component = injector.getComponent("queryBuilder");
    }

    @Test
    public void generate()
    {
        Response response = mock(Response.class);
        component.generate(getSpyMockRequest("queryBuilder", ImmutableMap.of(
                "sql", "select * from testtable limit 1",
                RestApiConstants.TIMESTAMP_PARAM, "" + new Date().getTime())), response, injector);

        verify(response).sendAsJson(any(JsonApiModel.class));
    }

}