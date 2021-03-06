package com.developmentontheedge.be5.metadata.serialization;

import com.developmentontheedge.be5.metadata.exception.ProjectLoadException;
import com.developmentontheedge.be5.metadata.exception.ProjectSaveException;
import com.developmentontheedge.be5.metadata.model.Project;
import com.developmentontheedge.be5.metadata.util.JULLogger;
import com.developmentontheedge.be5.metadata.util.NullLogger;
import com.developmentontheedge.be5.metadata.util.ProjectTestUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.Collections;
import java.util.logging.Logger;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class ModuleLoader2Test
{
    @Rule
    public TemporaryFolder tmp = new TemporaryFolder();

    private Path path;

    @Before
    public void setUp() throws Exception
    {
        path = tmp.newFolder().toPath();
        Serialization.save(ProjectTestUtils.getProject("test"), path);
    }

    @Test
    public void loadAllProjectsTest() throws IOException
    {
        ModuleLoader2.loadAllProjects(singletonList(path.resolve("project.yaml").toUri().toURL()), new NullLogger());
    }

    @Test
    public void loadAllProjectsTestWithDev() throws IOException, ProjectLoadException
    {
        try (PrintWriter out = new PrintWriter(path.resolve("dev.yaml").toFile()))
        {
            out.println("paths:" + "\n    test: " + path.toAbsolutePath());
        }

        ModuleLoader2.loadAllProjects(singletonList(path.resolve("project.yaml").toUri().toURL()), new NullLogger());
        ModuleLoader2.readDevPathsToSourceProjects(path.resolve("dev.yaml").toUri().toURL(), new NullLogger());

        assertTrue(ModuleLoader2.getPathsToProjectsToHotReload().toString().startsWith("{test="));
        assertEquals(1, ModuleLoader2.getPathsToProjectsToHotReload().size());

        Project loadProject = ModuleLoader2.findAndLoadProjectWithModules(true, new NullLogger());
        assertEquals("test", loadProject.getAppName());

        ModuleLoader2.getFileSystem(loadProject, "test");

        ModuleLoader2.clear();
    }

}
