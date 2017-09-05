package com.developmentontheedge.be5.api.components.impl

import com.developmentontheedge.be5.api.Request
import com.developmentontheedge.be5.api.Response
import com.developmentontheedge.be5.api.services.Meta
import com.developmentontheedge.be5.components.RestApiConstants
import com.developmentontheedge.be5.components.impl.DocumentGenerator
import com.developmentontheedge.be5.env.Inject
import com.developmentontheedge.be5.model.TablePresentation
import com.developmentontheedge.be5.testutils.TestTableQueryDBTest
import com.google.common.collect.ImmutableMap
import org.junit.Test


import static org.junit.Assert.*
import static org.mockito.Mockito.mock

class DocumentGeneratorTest extends TestTableQueryDBTest
{
    @Inject private Meta meta

    private Response response = mock(Response.class)
    private Request request = getSpyMockRequest("",
                ImmutableMap.of(RestApiConstants.ENTITY, "testtable", RestApiConstants.QUERY, "All records"))

    @Test
    void getTablePresentation()
    {
        TablePresentation testtable = new DocumentGenerator(request, response, injector).getTablePresentation(
                meta.getQuery("testtable", "All records", Collections.singletonList("Guest")), new HashMap<>())

        assertEquals("testtable: All records", testtable.getTitle())
    }

    @Test
    void testLinkQuick()
    {
        TablePresentation testtable = new DocumentGenerator(request, response, injector).getTablePresentation(
                meta.getQuery("testtable", "LinkQuick", Collections.singletonList("SystemDeveloper")), new HashMap<>())

        assertEquals("testtable: LinkQuick", testtable.getTitle())
    }

}