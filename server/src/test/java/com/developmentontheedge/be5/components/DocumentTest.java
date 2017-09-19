package com.developmentontheedge.be5.components;

import com.developmentontheedge.be5.api.Component;
import com.developmentontheedge.be5.api.Response;
import com.developmentontheedge.be5.env.Inject;
import com.developmentontheedge.be5.env.Injector;
import com.developmentontheedge.be5.model.jsonapi.ResourceData;
import com.developmentontheedge.be5.test.SqlMockOperationTest;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;
import java.util.Map;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMapOf;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class DocumentTest extends SqlMockOperationTest
{
    @Inject private Injector injector;
    private Component component;

    @Before
    public void init(){
        component = injector.getComponent("document");
    }

    @Test
    public void generate() throws Exception {
        Response response = mock(Response.class);

        component.generate(getSpyMockRequest("", ImmutableMap.of(
                RestApiConstants.ENTITY,"testtable",
                RestApiConstants.TIMESTAMP_PARAM,"" + new Date().getTime(),
                RestApiConstants.QUERY,"All records")), response, injector);

        verify(response).sendAsJson(any(ResourceData.class), any(Map.class), anyMapOf(String.class, String.class));
    }

}