package com.developmentontheedge.be5.api

import com.developmentontheedge.be5.api.impl.RequestImpl
import com.developmentontheedge.be5.model.Base64File
import com.developmentontheedge.be5.test.TestUtils
import com.google.common.collect.ImmutableMap
import org.junit.Before
import org.junit.Test

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpSession

import static org.junit.Assert.*
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.when


class RequestTest extends TestUtils
{
    HttpServletRequest rawRequest

    @Before
    void setUp()
    {
        rawRequest = mock(HttpServletRequest.class)
        when(rawRequest.getSession()).thenReturn(mock(HttpSession.class))
    }

    @Test
    void getValues()
    {
        Request req = getSpyMockRecForOp("testtableAdmin", "All records", "Insert", "",
                "{'name':'test','value':1,'accept':true}")

        assertEquals(ImmutableMap.of("name", "test", "value", "1", "accept", "true"),
                req.getValuesFromJson(RestApiConstants.VALUES))
    }

    @Test
    void testBase64File()
    {
        String text = "Simple text"
        String frontendEncoded = "data:;base64," + Base64.getEncoder().encodeToString(text.getBytes("UTF-8"))
        Request req = getSpyMockRecForOp("testtableAdmin", "All records", "Insert", "",
                "{'base64':{'type':'Base64File','data':'${frontendEncoded}','name':'test.txt'}}")
        Base64File base64File = req.getValuesFromJson("values").get("base64")

        assertEquals(text, new String(base64File.data))
        assertEquals("", new String(base64File.mimeTypes))
    }

    @Test
    void testBase64FileWithMimeTypes()
    {
        String text = "test opendocument.text"
        String frontendEncoded = "data:application/vnd.oasis.opendocument.text;base64," + Base64.getEncoder().encodeToString(text.getBytes("UTF-8"))
        Request req = getSpyMockRecForOp("testtableAdmin", "All records", "Insert", "",
                "{'base64':{'type':'Base64File','data':'${frontendEncoded}','name':'test.txt'}}")
        Base64File base64File = req.getValuesFromJson("values").get("base64")

        assertEquals(text, new String(base64File.data))
        assertEquals("application/vnd.oasis.opendocument.text", new String(base64File.mimeTypes))
    }

    @Test
    void name()
    {
        when(rawRequest.getParameterValues("ids[]")).thenReturn(["1", "2"] as String[])
        def req = new RequestImpl(rawRequest, "test")

        assertEquals(["1", "2"], req.getList("ids"))
    }
}