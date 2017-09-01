package com.developmentontheedge.be5.components.impl;

import com.developmentontheedge.be5.api.Request;
import com.developmentontheedge.be5.api.Response;
import com.developmentontheedge.be5.api.services.Meta;
import com.developmentontheedge.be5.components.RestApiConstants;
import com.developmentontheedge.be5.env.Inject;
import com.developmentontheedge.be5.env.Injector;
import com.developmentontheedge.be5.model.TablePresentation;
import com.developmentontheedge.be5.test.Be5ProjectDBTest;
import com.google.common.collect.ImmutableMap;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

public class DocumentGeneratorTest extends Be5ProjectDBTest
{
    @Inject private Meta meta;
    @Inject private Injector injector;
    private Response response = mock(Response.class);
    private Request request = getSpyMockRequest("",
                ImmutableMap.of(RestApiConstants.ENTITY, "testtable", RestApiConstants.QUERY, "All records"));

    @Test
    public void getTablePresentation()
    {
        TablePresentation testtable = new DocumentGenerator(request, response, injector).getTablePresentation(
                meta.getQuery("testtable", "All records", Collections.singletonList("Guest")), new HashMap<>());

        assertEquals("testtable: All records", testtable.getTitle());
    }

    @Test
    public void testLinkQuick()
    {
        TablePresentation testtable = new DocumentGenerator(request, response, injector).getTablePresentation(
                meta.getQuery("testtable", "LinkQuick", Collections.singletonList("SystemDeveloper")), new HashMap<>());

        assertEquals("testtable: LinkQuick", testtable.getTitle());
    }

}