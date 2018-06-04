package com.developmentontheedge.be5.maven.generate;

import com.developmentontheedge.be5.maven.Be5Mojo;
import com.developmentontheedge.be5.metadata.scripts.generate.GenerateContext;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;


@Mojo( name = "generate-context")
public class GenerateContextMojo extends Be5Mojo
{
    @Parameter(property = "GENERATE_CONTEXT_PATH")
    private String generateContextPath;

    @Parameter(property = "SKIP_GENERATE_CONTEXT")
    private boolean skipGenerateContextPath = false;

    @Override
    public void execute() throws MojoFailureException
    {
        new GenerateContext()
                .setBe5ProjectPath(projectPath.getPath())
                .setProfileName(connectionProfileName)
                .setGenerateContextPath(generateContextPath)
                .setSkipGenerateContextPath(skipGenerateContextPath)
                .execute();
    }
}