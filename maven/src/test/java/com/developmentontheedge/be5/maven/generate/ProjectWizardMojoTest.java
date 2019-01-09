package com.developmentontheedge.be5.maven.generate;

import com.developmentontheedge.be5.maven.TestMavenUtils;
import com.developmentontheedge.be5.metadata.exception.ProjectLoadException;
import com.developmentontheedge.be5.metadata.model.Project;
import com.developmentontheedge.be5.metadata.serialization.Serialization;
import com.developmentontheedge.be5.metadata.util.NullLogger;
import org.junit.Test;

import java.io.OutputStream;
import java.io.PrintStream;

import static org.junit.Assert.assertEquals;

public class ProjectWizardMojoTest extends TestMavenUtils
{
    @Test
    public void execute()
    {
        ProjectWizardMojo mojo = new ProjectWizardMojo();
        mojo.projectPath = tpmProjectPath.toFile();
        mojo.connectionProfileName = profileTestMavenPlugin;

        mojo.inputStream = inputStream("test-name\n5\n5\n");
        mojo.printStream = new PrintStream(new OutputStream() {public void write(int b) {}});
        mojo.logger = new NullLogger();
        mojo.execute();

        assertEquals("test-name", getProject().getName());
    }

    private Project getProject()
    {
        try
        {
            return Serialization.load(tpmProjectPath.toAbsolutePath());
        }
        catch (ProjectLoadException e)
        {
            throw new RuntimeException(e);
        }
    }
}
