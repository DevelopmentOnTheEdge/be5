package com.developmentontheedge.be5.maven.generate;

import com.developmentontheedge.be5.maven.TestMavenUtils;
import org.junit.Test;

import java.io.IOException;


public class GroovyDSLGeneratorMavenTest extends TestMavenUtils
{
    @Test
    public void test() throws IOException
    {
        GroovyDSLGeneratorMojo mojo = new GroovyDSLGeneratorMojo();
        mojo.projectPath = tpmProjectPath.toFile();
        mojo.connectionProfileName = profileTestMavenPlugin;

        mojo.fileName = tpmProjectPath.toAbsolutePath().toString() + "/";

        mojo.execute();
    }
}