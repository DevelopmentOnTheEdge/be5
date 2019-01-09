package com.developmentontheedge.be5.maven;

import com.developmentontheedge.be5.metadata.util.NullLogger;
import org.junit.Test;


public class AppValidateMavenTest extends TestMavenUtils
{
    @Test
    public void validate()
    {
        AppValidateMojo mojo = new AppValidateMojo();

        mojo.projectPath = tpmProjectPath.toFile();
        mojo.logger = new NullLogger();
        mojo.execute();
    }
}
