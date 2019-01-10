package com.developmentontheedge.be5.metadata.scripts.wizard;

import com.developmentontheedge.be5.metadata.exception.ProjectLoadException;
import com.developmentontheedge.be5.metadata.model.Project;
import com.developmentontheedge.be5.metadata.serialization.Serialization;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.TreeSet;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;


public class ProjectWizardTest
{
    @Rule
    public TemporaryFolder tmp = new TemporaryFolder();

    protected Path tpmProjectPath;

    private ProjectWizard projectWizard;

    @Before
    public void setUpAppTools() throws Exception
    {
        tpmProjectPath = tmp.newFolder().toPath();
        projectWizard = new ProjectWizard()
                .setBe5ProjectPath(tpmProjectPath.toAbsolutePath().toString())
                .setPrintStream(new PrintStream(new OutputStream() {public void write(int b) {}}));
    }

    @Test
    public void simple()
    {
        projectWizard
                .setInputStream("test-name\n5\n5\n")
                .execute();

        assertEquals("test-name", getProject().getName());
    }

    @Test
    public void roles()
    {
        projectWizard
                .setInputStream("test-name\n2\n1\n2\n2\nNewRole\n5")
                .execute();

        assertEquals(new TreeSet<>(Arrays.asList("Administrator", "Guest", "NewRole")),
                getProject().getRoles());
    }

    @Test
    public void languages()
    {
        projectWizard
                .setInputStream("test-name\n3\n1\nen\n5")
                .execute();

        assertArrayEquals(new String[]{"en", "ru"},
                getProject().getLanguages());
    }

    @Test
    public void modules()
    {
        projectWizard
                .setInputStream("test-name\n4\n2\ntest-module\n5")
                .execute();

        assertEquals(Arrays.asList("core", "test-module"),
                getProject().getModules().names().toList());
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
