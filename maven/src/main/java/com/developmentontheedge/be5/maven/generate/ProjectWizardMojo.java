package com.developmentontheedge.be5.maven.generate;

import com.developmentontheedge.be5.maven.Be5Mojo;
import com.developmentontheedge.be5.metadata.scripts.wizard.ProjectWizard;
import org.apache.maven.plugins.annotations.Mojo;

import java.io.InputStream;
import java.io.PrintStream;


@Mojo(name = "wizard")
public class ProjectWizardMojo extends Be5Mojo
{
    InputStream inputStream = System.in;
    PrintStream printStream = System.out;

    @Override
    public void execute()
    {
        init();
        new ProjectWizard()
                .setBe5ProjectPath(projectPath.getPath())
                .setProfileName(connectionProfileName)
                .setConnectionPassword(connectionPassword)
                .setLogPath(logPath)
                .setLogger(logger)
                .setDebug(debug)
                .setInputStream(inputStream)
                .setPrintStream(printStream)
                .execute();
    }
}
