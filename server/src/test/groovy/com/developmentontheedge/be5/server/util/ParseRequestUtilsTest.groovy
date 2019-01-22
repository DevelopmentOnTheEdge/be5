package com.developmentontheedge.be5.server.util

import com.developmentontheedge.be5.server.model.Base64File
import com.developmentontheedge.be5.test.BaseTestUtils
import com.google.common.collect.ImmutableMap
import org.junit.Test

import static org.junit.Assert.assertEquals

class ParseRequestUtilsTest extends BaseTestUtils
{
    @Test
    void getContextParams()
    {
        assertEquals(ImmutableMap.of("name", "test", "value", "1", "accept", "true"),
                ParseRequestUtils.getContextParams(doubleQuotes("{'name':'test','value':1,'accept':true}")))
    }

    @Test
    void testBase64File()
    {
        String text = "Simple text"
        String frontendEncoded = "data:;base64," +
                Base64.getEncoder().encodeToString(text.getBytes("UTF-8"))
        Map<String, String[]> map = new HashMap<>()
        map.put("base64", [doubleQuotes("{'type':'Base64File','data':'${frontendEncoded}','name':'test.txt'}")] as String[])
        Base64File base64File = (Base64File) ParseRequestUtils.getFormValues(map).get("base64")

        assertEquals(text, new String(base64File.data))
        assertEquals("", new String(base64File.mimeTypes))
    }

    @Test
    void testBase64FileWithMimeTypes()
    {
        String text = "test opendocument.text"
        String frontendEncoded = "data:application/vnd.oasis.opendocument.text;base64," +
                Base64.getEncoder().encodeToString(text.getBytes("UTF-8"))
        Map<String, String[]> map = new HashMap<>()
        map.put("base64", [doubleQuotes("{'type':'Base64File','data':'${frontendEncoded}','name':'test.txt'}")] as String[])
        Base64File base64File = (Base64File) ParseRequestUtils.getFormValues(map).get("base64")

        assertEquals(text, new String(base64File.data))
        assertEquals("application/vnd.oasis.opendocument.text", new String(base64File.mimeTypes))
    }
}
