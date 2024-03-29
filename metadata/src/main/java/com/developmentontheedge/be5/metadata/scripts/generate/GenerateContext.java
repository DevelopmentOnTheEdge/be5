package com.developmentontheedge.be5.metadata.scripts.generate;

import com.developmentontheedge.be5.metadata.exception.ProjectLoadException;
import com.developmentontheedge.be5.metadata.model.BeConnectionProfile;
import com.developmentontheedge.be5.metadata.scripts.ScriptException;
import com.developmentontheedge.be5.metadata.scripts.ScriptSupport;
import com.developmentontheedge.be5.metadata.sql.Rdbms;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.logging.Logger;
import java.util.stream.Collectors;


public class GenerateContext extends ScriptSupport<GenerateContext>
{
    public static final String ENVIRONMENT_NAME = "environmentName";
    private static final Logger log = Logger.getLogger(GenerateContext.class.getName());

    private String generateContextPath;

    private boolean skipGenerateContextPath = false;

    private String generateFilePath;
    private String environmentName = "test";

    @Override
    public void execute()
    {
        if (skipGenerateContextPath)
        {
            logger.info("Generate context.xml skipped.");
            return;
        }

        if (connectionProfileName == null)
        {
            logger.info("Generate context.xml skipped - BE5_PROFILE not specified.");
            return;
        }

        generateFilePath = generateContextPath + "/context.xml";

        if (generateContextPath == null) throw new ScriptException("generateContextPath is null");

        File file = Paths.get(generateFilePath).toFile();

        if (file.exists() && !file.isDirectory())
        {
            logger.info("Generate context.xml skipped, file exists: " + generateFilePath);
            return;
        }

        initProject();

        if (connectionProfileName != null)
        {
            be5Project.setConnectionProfileName(connectionProfileName);
        }

        try
        {
            createFile();
        }
        catch (IOException | ProjectLoadException e)
        {
            e.printStackTrace();
        }
    }

    private void createFile() throws IOException, ScriptException, ProjectLoadException
    {
        String text;

        InputStream resource = getClass().getClassLoader().getResourceAsStream("generate-context/context.xml");

        try (BufferedReader br = new BufferedReader(new InputStreamReader(resource, StandardCharsets.UTF_8)))
        {
            text = br.lines().collect(Collectors.joining("\n"));
        }
        String resultContext = replacePlaceholders(text);

        Paths.get(generateContextPath).toFile().mkdirs();
        PrintWriter writer = new PrintWriter(generateFilePath, "UTF-8");
        writer.println(resultContext);
        writer.close();

        logger.info("context.xml created in " + generateContextPath);
    }

    private String replacePlaceholders(String text) throws ScriptException, ProjectLoadException
    {
        BeConnectionProfile prof = be5Project.getConnectionProfile();
        if (prof == null)
        {
            throw new ScriptException("Connection profile is required for 'generate-context'");
        }

        String sessionStore = prof.getProperty( "SESSION_STORE" );
        if( sessionStore != null )
        {
            sessionStore = "<Manager pathname=\"" + sessionStore + "\"/>";
        }
        else
        {
            sessionStore = "";   
        }  

        return text.
                replaceAll("PROJECT_NAME", be5Project.getName()).
                replaceAll("USERNAME", prof.getUsername()).
                replaceAll("PASSWORD", connectionPassword != null ? connectionPassword : prof.getPassword()).
                replaceAll("URL", prof.getConnectionUrl()).
                replaceAll("DRIVER_DEFINITION", prof.getDriverDefinition()).
                replaceAll("VALIDATION_QUERY", Rdbms.getRdbms(prof.getConnectionUrl()).getValidationQuery()).
                replaceAll("DRIVER_CLASS", Rdbms.getRdbms(prof.getConnectionUrl()).getDriverDefinition()).
                replaceAll("<!--PARAMETERS-->", getParameters()).
                replaceAll("<!--SESSION_STORE-->", sessionStore );
    }

    private String getParameters()
    {
        return "<Parameter name=\"" + ENVIRONMENT_NAME + "\" value=\"" + environmentName + "\" override=\"false\"/>";
    }

    public GenerateContext setGenerateContextPath(String generateContextPath)
    {
        this.generateContextPath = generateContextPath;
        return this;
    }

    public GenerateContext setSkipGenerateContextPath(boolean skipGenerateContextPath)
    {
        this.skipGenerateContextPath = skipGenerateContextPath;
        return this;
    }

    public GenerateContext setEnvironmentName(String environmentName)
    {
        this.environmentName = environmentName;
        return this;
    }

    @Override
    public GenerateContext me()
    {
        return this;
    }
}
