package com.developmentontheedge.be5.metadata.scripts.generate;

import com.developmentontheedge.be5.metadata.scripts.ScriptTestUtils;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;

import static org.junit.Assert.assertTrue;


public class GenerateContextTest extends ScriptTestUtils
{
    @Test
    public void execute() throws Exception
    {
        Path targetPath = tmp.newFolder().toPath();
        new GenerateContext()
                .setBe5ProjectPath(tpmProjectPath)
                .setProfileName(profileTestMavenPlugin)
                .setGenerateContextPath(targetPath.toString())
                .execute();

        String result = Files.readAllLines(targetPath.resolve("context.xml")).stream().collect(Collectors.joining("\n"));

        assertTrue(result.contains("<Resource name=\"jdbc/test\""));
        assertTrue(result.contains("username=\"sa\""));
        assertTrue(result.contains("password=\"\""));
        assertTrue(result.contains("url=\"jdbc:h2:~/" + profileTestMavenPlugin + "\""));
        assertTrue(result.contains("driverClassName=\"org.h2.Driver\""));
        assertTrue(result.contains("<Parameter name=\"environmentName\" value=\"test\""));
    }

    @Test
    public void prod() throws Exception
    {
        Path targetPath = tmp.newFolder().toPath();
        new GenerateContext()
                .setBe5ProjectPath(tpmProjectPath)
                .setProfileName(profileTestMavenPlugin)
                .setGenerateContextPath(targetPath.toString())
                .setEnvironmentName("prod")
                .execute();
        String result = Files.readAllLines(targetPath.resolve("context.xml")).stream().collect(Collectors.joining("\n"));
        assertTrue(result.contains("<Parameter name=\"environmentName\" value=\"prod\""));
    }

}
