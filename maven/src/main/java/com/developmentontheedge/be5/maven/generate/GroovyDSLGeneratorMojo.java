package com.developmentontheedge.be5.maven.generate;

import com.developmentontheedge.be5.maven.MavenLogger;
import com.developmentontheedge.be5.metadata.scripts.generate.GroovyDSLGenerator;
import com.developmentontheedge.be5.metadata.util.ProcessController;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;


@Mojo(name = "generate-groovy-dsl")
public class GroovyDSLGeneratorMojo extends AbstractMojo
{
    protected ProcessController logger = new MavenLogger(getLog());

    @Parameter(property = "FILE_NAME")
    String fileName;

    @Override
    public void execute()
    {
        new GroovyDSLGenerator()
                .setFileName(fileName)
                .setLogger(logger)
                .execute();
    }
}
