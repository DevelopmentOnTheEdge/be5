package com.developmentontheedge.be5.components;

import com.developmentontheedge.be5.api.Component;
import com.developmentontheedge.be5.api.Response;
import com.developmentontheedge.be5.api.RestApiConstants;
import com.developmentontheedge.be5.inject.Inject;
import com.developmentontheedge.be5.inject.Injector;
import com.developmentontheedge.be5.model.jsonapi.JsonApiModel;
import com.developmentontheedge.be5.test.SqlMockOperationTest;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;


public class TableTest extends SqlMockOperationTest
{
    @Inject private Injector injector;
    private Component component;

    @Before
    public void init()
    {
        component = (Component)injector.getComponent("table");
    }

    @Test
    public void generate()
    {
        Response response = mock(Response.class);

        component.generate(getSpyMockRequest("", ImmutableMap.of(
                RestApiConstants.ENTITY,"testtable",
                RestApiConstants.TIMESTAMP_PARAM,"" + new Date().getTime(),
                RestApiConstants.QUERY,"All records")), response, injector);

        verify(response).sendAsJson(any(JsonApiModel.class));
    }

}