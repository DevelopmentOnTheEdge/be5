package com.developmentontheedge.be5.maven;

import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

public class GenerateContextTest extends TestUtils
{
    @Test
    public void execute() throws Exception
    {
        Path path = tmp.newFolder().toPath();
        new GenerateContext()
                .setBe5Project(project)
                .setProfileName(profileTestMavenPlugin)
                .setGeneratePath(path.toString())
                .execute();


        String result = Files.readAllLines(path.resolve("context.xml")).stream().collect(Collectors.joining("\n"));


        assertTrue(result.contains("<Resource name=\"jdbc/test\""));
        assertTrue(result.contains("username=\"sa\""));
        assertTrue(result.contains("password=\"\""));
        assertTrue(result.contains("url=\"jdbc:h2:~/profileTestMavenPlugin\""));
    }

}