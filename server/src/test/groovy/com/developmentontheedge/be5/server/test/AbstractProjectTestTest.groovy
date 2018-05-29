package com.developmentontheedge.be5.server.test

import com.developmentontheedge.be5.web.Request
import org.junit.Test

import static org.junit.Assert.*


class AbstractProjectTestTest extends ServerBe5ProjectTest
{
    @Test
    void test()
    {
        Request request = getSpyMockRecForOp("testtableAdmin", "All records", "TestOperation", "0",
                "", ["companyID": "12"])
        assertEquals "12", request.getAttribute("companyID")
    }

    @Test
    void testSession()
    {
        Request request = getSpyMockRecForOp("testtableAdmin", "All records", "TestOperation", "0",
                "", ["companyID": "12"])
        def session = request.getSession()
        assertEquals "12", session["companyID"]
    }
}
