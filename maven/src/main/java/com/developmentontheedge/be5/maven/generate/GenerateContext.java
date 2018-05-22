package com.developmentontheedge.be5.maven.generate;

import com.developmentontheedge.be5.maven.Be5Mojo;
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
import java.util.logging.Logger;
import java.util.stream.Collectors;


@Mojo( name = "generate-context")
public class GenerateContext extends Be5Mojo<GenerateContext>
{
    private static final Logger log = Logger.getLogger(GenerateContext.class.getName());

    @Parameter(property = "GENERATE_CONTEXT_PATH")
    private String generateContextPath;

    @Parameter(property = "SKIP_GENERATE_CONTEXT")
    private boolean skipGenerateContextPath = false;

    private String generateFilePath;

    @Override
    public void execute() throws MojoFailureException
    {
        if(skipGenerateContextPath)
        {
            log.info("Generate context.xml skipped.");
            return;
        }

        if(connectionProfileName == null)
        {
            log.info("Generate context.xml skipped - BE5_PROFILE not specified.");
            return;
        }

        generateFilePath = generateContextPath + "/context.xml";

        if(generateContextPath == null)throw new MojoFailureException("generateContextPath is null");

        File file = Paths.get(generateFilePath).toFile();

        if(file.exists() && !file.isDirectory())
        {
            log.info("Generate context.xml skipped, file exists: " + generateFilePath);
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

        //InputStream resource = getClass().getClassLoader().getResourceAsStream("generate-context/context.xml");
        InputStream resource = getClass().getClassLoader().getResourceAsStream("generate-context/tomcat-pool.xml");

        try(BufferedReader br = new BufferedReader(new InputStreamReader(resource)))
        {
            text = br.lines().collect(Collectors.joining("\n"));
        }
        String resultContext = replacePlaceholders(text);

        Paths.get(generateContextPath).toFile().mkdirs();
        PrintWriter writer = new PrintWriter(generateFilePath, "UTF-8");
        writer.println(resultContext);
        writer.close();

        getLog().info("context.xml created in " + generateContextPath);
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
                replaceAll("PASSWORD", connectionPassword != null ? connectionPassword : prof.getPassword()).
                replaceAll("URL", prof.getConnectionUrl());
    }

    public GenerateContext setGenerateContextPath(String generateContextPath)
    {
        this.generateContextPath = generateContextPath;
        return this;
    }

    @Override protected GenerateContext me() {
        return this;
    }
}
