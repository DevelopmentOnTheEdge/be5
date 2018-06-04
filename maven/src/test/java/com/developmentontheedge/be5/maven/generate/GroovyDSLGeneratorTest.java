package com.developmentontheedge.be5.maven.generate;

import com.developmentontheedge.be5.maven.TestUtils;
import org.junit.Test;

import java.io.IOException;


public class GroovyDSLGeneratorTest extends TestUtils
{
    @Test
    public void test() throws IOException
    {
        GroovyDSLGeneratorMojo mojo = new GroovyDSLGeneratorMojo();
        mojo.fileName = tpmProjectPath.toAbsolutePath().toString() + "/";

        mojo.execute();
    }
}