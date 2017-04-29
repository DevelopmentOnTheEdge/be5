package com.developmentontheedge.be5.api.services.impl;

import com.developmentontheedge.be5.metadata.exception.ProjectLoadException;
import com.developmentontheedge.be5.metadata.model.Project;
import com.developmentontheedge.be5.metadata.serialization.LoadContext;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ProjectLoadTest
{
    private static ProjectProviderImpl projectProvider = new ProjectProviderImpl();
    private static List<Project> modulesAndProject;

    @BeforeClass
    public static void load() throws IOException, URISyntaxException, ProjectLoadException
    {
        modulesAndProject = projectProvider.loadModulesAndProject(new LoadContext());
    }

    @Test
    public void testLoadProject() throws IOException, URISyntaxException, ProjectLoadException
    {
        Project project = projectProvider.getProject(modulesAndProject);

        assertNotNull(project);
        assertEquals("testProject", project.getName());
    }

    @Test
    @Ignore
    public void testLoadModuleCore() throws IOException, URISyntaxException, ProjectLoadException
    {
        Project project = projectProvider.getProject(modulesAndProject);
        List<Project> modules = projectProvider.getModulesForProject(project, modulesAndProject);
        assertEquals(1, modules.size());
        assertEquals("core", modules.get(0).getName());
    }

    @Test
    public void testReadDevPathsToSourceProjects() throws IOException, URISyntaxException, ProjectLoadException
    {
        Map<String, String> pathsToSourceProjects = new ProjectProviderImpl().readDevPathsToSourceProjects();
        assertEquals(1, pathsToSourceProjects.size());
        assertEquals("src/test/resources/project.yaml", pathsToSourceProjects.get("testProject"));
    }

}
