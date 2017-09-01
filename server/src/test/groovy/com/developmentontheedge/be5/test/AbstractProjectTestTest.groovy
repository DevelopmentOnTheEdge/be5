package com.developmentontheedge.be5.test

import com.developmentontheedge.be5.api.Request
import org.junit.Test

import static org.junit.Assert.*


class AbstractProjectTestTest extends Be5ProjectTest
{
    @Test
    void name() throws Exception {
        Request request = getSpyMockRecForOp("testtableAdmin", "All records", "TestOperation", "0",
                "", ["companyID": "12"])
        assertEquals "12", request.getAttribute("companyID")
    }
}
