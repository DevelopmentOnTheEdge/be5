package com.developmentontheedge.be5.server.util

import com.developmentontheedge.be5.server.model.Base64File
import com.google.common.collect.ImmutableMap
import org.junit.Test

import static org.junit.Assert.assertEquals

class ParseRequestUtilsTest
{
    @Test
    void getValues()
    {
        assertEquals(ImmutableMap.of("name", "test", "value", "1", "accept", "true"),
                ParseRequestUtils.getValuesFromJson("{'name':'test','value':1,'accept':true}"))
    }

    @Test
    void testBase64File()
    {
        String text = "Simple text"
        String frontendEncoded = "data:;base64," +
                Base64.getEncoder().encodeToString(text.getBytes("UTF-8"))
        def values = "{'base64':{'type':'Base64File','data':'${frontendEncoded}','name':'test.txt'}}"
        Base64File base64File = (Base64File) ParseRequestUtils.getValuesFromJson(values).get("base64")

        assertEquals(text, new String(base64File.data))
        assertEquals("", new String(base64File.mimeTypes))
    }

    @Test
    void testBase64FileWithMimeTypes()
    {
        String text = "test opendocument.text"
        String frontendEncoded = "data:application/vnd.oasis.opendocument.text;base64," +
                Base64.getEncoder().encodeToString(text.getBytes("UTF-8"))
        def value = "{'base64':{'type':'Base64File','data':'${frontendEncoded}','name':'test.txt'}}"
        Base64File base64File = (Base64File) ParseRequestUtils.getValuesFromJson(value).get("base64")

        assertEquals(text, new String(base64File.data))
        assertEquals("application/vnd.oasis.opendocument.text", new String(base64File.mimeTypes))
    }
}
