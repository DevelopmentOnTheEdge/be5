package com.developmentontheedge.be5.api

import com.developmentontheedge.be5.components.RestApiConstants
import com.developmentontheedge.be5.test.TestUtils
import com.google.common.collect.ImmutableMap
import org.junit.Test

import static org.junit.Assert.*


class RequestTest extends TestUtils
{
    @Test
    void getValues() throws Exception
    {
        Request req = getSpyMockRecForOp("testtableAdmin", "All records", "Insert", "",
                "{'name':'test','value':1,'accept':true}")

        assertEquals(ImmutableMap.of("name", "test", "value", "1", "accept", "true"),
                req.getValuesFromJson(RestApiConstants.VALUES))
    }

}