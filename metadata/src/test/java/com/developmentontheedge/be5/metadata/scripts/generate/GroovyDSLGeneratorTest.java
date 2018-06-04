package com.developmentontheedge.be5.metadata.scripts.generate;


import com.developmentontheedge.be5.metadata.scripts.ScriptTestUtils;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;


public class GroovyDSLGeneratorTest extends ScriptTestUtils
{
    @Test
    public void test() throws IOException
    {
        GroovyDSLGenerator groovyDSLGenerator = new GroovyDSLGenerator();
        groovyDSLGenerator.fileName = tpmProjectPath.toAbsolutePath().toString() + "/";
        groovyDSLGenerator.execute();

        String result = readFile(tpmProjectPath.toAbsolutePath().toString() + "/GroovyDSL.gdsl", Charsets.UTF_8);

        URL url = Resources.getResource("gdsl/test.txt");
        String test = Resources.toString(url, Charsets.UTF_8);

        assertEquals(test, result);
    }

    private static String readFile(String path, Charset encoding) throws IOException
    {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }
}