package com.developmentontheedge.be5.server.util;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.*;

public class IOUtilsTest
{
    @Test
    public void test() throws IOException
    {
        String initialString = "text\nline2";
        InputStream inputStream = new ByteArrayInputStream(initialString.getBytes());
        assertEquals(initialString, IOUtils.toString(inputStream, StandardCharsets.UTF_8));
    }

}