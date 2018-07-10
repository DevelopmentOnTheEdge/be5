package com.developmentontheedge.be5.maven.generate;

import com.developmentontheedge.be5.maven.TestMavenUtils;
import org.junit.Test;

import java.nio.file.Path;


public class GenerateContextMavenTest extends TestMavenUtils
{
    @Test
    public void execute() throws Exception
    {
        Path targetPath = tmp.newFolder().toPath();
        GenerateContextMojo mojo = new GenerateContextMojo();

        mojo.projectPath = tpmProjectPath.toFile();
        mojo.connectionProfileName = profileTestMavenPlugin;

        mojo.generateContextPath = targetPath.toString();
        mojo.skipGenerateContextPath = false;

        mojo.execute();
    }

}