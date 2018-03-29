package com.developmentontheedge.be5.api

import com.developmentontheedge.be5.api.impl.model.Base64File
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
}