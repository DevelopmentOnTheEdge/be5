package com.developmentontheedge.be5.maven;

import com.developmentontheedge.be5.metadata.model.BeConnectionProfile;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.stream.Collectors;


@Mojo( name = "generate-context")
public class GenerateContext extends Be5Mojo<GenerateContext>
{
    @Parameter(property = "GENERATE_PATH")
    private String generatePath;

    private String generateFilePath;

    @Override
    public void execute() throws MojoFailureException
    {
        generateFilePath = generatePath + "/context.xml";

        if(generatePath == null)throw new MojoFailureException("generatePath is null");

        File file = Paths.get(generateFilePath).toFile();

        if(file.exists() && !file.isDirectory())
        {
            System.out.println("Generate skipped, file exists: " + generateFilePath);
            return;
        }

        init();

        try
        {
            createFile();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private void createFile() throws IOException, MojoFailureException
    {
        String text;

        InputStream resource = getClass().getClassLoader().getResourceAsStream("generate-context/context.xml");

        try(BufferedReader br = new BufferedReader(new InputStreamReader(resource)))
        {
            text = br.lines().collect(Collectors.joining("\n"));
        }

        Paths.get(generatePath).toFile().mkdirs();

        PrintWriter writer = new PrintWriter(generateFilePath, "UTF-8");
        writer.println(replacePlaceholders(text));
        writer.close();

        getLog().info("context.xml created in " + generatePath);
    }

    private String replacePlaceholders(String text) throws MojoFailureException
    {
        BeConnectionProfile prof = be5Project.getConnectionProfile();
        if(prof == null)
        {
            throw new MojoFailureException("Connection profile is required for 'generate-context'");
        }

        return text.
                replaceAll("PROJECT_NAME", be5Project.getName()).
                replaceAll("USERNAME", prof.getUsername()).
                replaceAll("PASSWORD", prof.getPassword()).
                replaceAll("URL", prof.getConnectionUrl());
    }

    public GenerateContext setGeneratePath(String generatePath)
    {
        this.generatePath = generatePath;
        return this;
    }

    @Override protected GenerateContext me() {
        return this;
    }
}
