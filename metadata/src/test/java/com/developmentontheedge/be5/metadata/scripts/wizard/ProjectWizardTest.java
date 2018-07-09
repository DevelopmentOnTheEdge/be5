package com.developmentontheedge.be5.metadata.scripts.wizard;

import com.developmentontheedge.be5.metadata.model.Project;
import com.developmentontheedge.be5.metadata.serialization.Serialization;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.nio.file.Path;

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
                .setBe5ProjectPath(tpmProjectPath.toAbsolutePath().toString());
    }

    @Test
    public void simple() throws Exception
    {
        projectWizard
                .setInputStream("test-name\n5\n5\n")
                .execute();

        assertEquals("test-name", getProject().getName());
    }

    @Test
    public void roles() throws Exception
    {
        projectWizard
                .setInputStream("test-name\n2\n1\n2\n2\nNewRole\n5")
                .execute();

        assertEquals(ImmutableSet.of("Administrator", "NewRole", "Guest"),
                getProject().getRoles());
    }

    @Test
    public void languages() throws Exception
    {
        projectWizard
                .setInputStream("test-name\n3\n1\nen\n5")
                .execute();

        assertArrayEquals(ImmutableList.of("en", "ru").toArray(),
                getProject().getLanguages());
    }

    @Test
    public void modules() throws Exception
    {
        projectWizard
                .setInputStream("test-name\n4\n2\ntest-module\n5")
                .execute();

        assertEquals(ImmutableList.of("core", "test-module"),
                getProject().getModules().names().toList());
    }

    private Project getProject() throws Exception
    {
        return Serialization.load(tpmProjectPath.toAbsolutePath());
    }
}