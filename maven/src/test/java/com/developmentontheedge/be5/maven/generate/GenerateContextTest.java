package com.developmentontheedge.be5.maven.generate;

import com.developmentontheedge.be5.maven.TestUtils;
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
        Path targetPath = tmp.newFolder().toPath();
        GenerateContext generateContext = new GenerateContext();
        generateContext.projectPath = tpmProjectPath.toFile();
        generateContext.connectionProfileName = profileTestMavenPlugin;
        generateContext.generateContextPath = targetPath.toString();

//        generateContext.execute();
//
//        String result = Files.readAllLines(targetPath.resolve("context.xml")).stream().collect(Collectors.joining("\n"));
//
//
//        assertTrue(result.contains("<Resource name=\"jdbc/test\""));
//        assertTrue(result.contains("username=\"sa\""));
//        assertTrue(result.contains("password=\"\""));
//        assertTrue(result.contains("url=\"jdbc:h2:~/profileTestMavenPlugin\""));
    }

}