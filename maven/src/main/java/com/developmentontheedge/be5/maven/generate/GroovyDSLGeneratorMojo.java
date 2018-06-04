package com.developmentontheedge.be5.maven.generate;

import com.developmentontheedge.be5.metadata.scripts.generate.GroovyDSLGenerator;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;


@Mojo(name = "generate-groovy-dsl")
public class GroovyDSLGeneratorMojo extends AbstractMojo
{
    @Parameter(property = "FILE_NAME")
    protected String fileName;

    @Override
    public void execute() throws MojoFailureException
    {
        new GroovyDSLGenerator()
                .setFileName(fileName)
                .execute();
    }
}
