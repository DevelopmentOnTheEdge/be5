package com.developmentontheedge.be5.maven.generate;

import com.developmentontheedge.be5.maven.TestMavenUtils;
import com.developmentontheedge.be5.metadata.util.NullLogger;
import org.junit.Test;

import java.io.IOException;


public class GroovyDSLGeneratorMavenTest extends TestMavenUtils
{
    @Test
    public void test()
    {
        GroovyDSLGeneratorMojo mojo = new GroovyDSLGeneratorMojo();
        mojo.projectPath = tpmProjectPath.toFile();
        mojo.connectionProfileName = profileTestMavenPlugin;

        mojo.fileName = tpmProjectPath.toAbsolutePath().toString() + "/";
        mojo.logger = new NullLogger();
        mojo.execute();
    }
}
