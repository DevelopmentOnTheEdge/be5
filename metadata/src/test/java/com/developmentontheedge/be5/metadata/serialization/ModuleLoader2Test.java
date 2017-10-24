package com.developmentontheedge.be5.metadata.serialization;

import com.developmentontheedge.be5.metadata.exception.ProjectLoadException;
import com.developmentontheedge.be5.metadata.exception.ProjectSaveException;
import com.developmentontheedge.be5.metadata.model.Project;
import com.developmentontheedge.be5.metadata.sql.Rdbms;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class ModuleLoader2Test
{
    @Rule
    public TemporaryFolder tmp = new TemporaryFolder();

    private Path path;
    private Project project;

    @Before
    public void setUp() throws Exception
    {
        path = tmp.newFolder().toPath();
        project = new Project( "test" );
        project.setRoles( Arrays.asList( "Administrator", "Guest", "User", "Operator" ) );
        project.setDatabaseSystem( Rdbms.POSTGRESQL );
        Serialization.save( project, path );
    }

    @Test
    public void loadAllProjectsTest() throws IOException, ProjectSaveException, ProjectLoadException
    {
        ModuleLoader2.loadAllProjects(Collections.singletonList(path.resolve("project.yaml").toUri().toURL()));
    }

    @Test
    public void loadAllProjectsTestWithDev() throws IOException, ProjectSaveException, ProjectLoadException
    {
        try( PrintWriter out = new PrintWriter( path.resolve("dev.yaml").toFile() ) )
        {
            out.println( "paths:" + "\n    test: " + path.toAbsolutePath() );
        }

        ModuleLoader2.loadAllProjects(Collections.singletonList(path.resolve("project.yaml").toUri().toURL()));
        ModuleLoader2.readDevPathsToSourceProjects(Collections.singletonList(path.resolve("dev.yaml").toUri().toURL()));

        assertTrue(ModuleLoader2.getPathsToProjectsToHotReload().toString().startsWith("{test="));
        assertEquals(1, ModuleLoader2.getPathsToProjectsToHotReload().size());

        Project loadProject = ModuleLoader2.findAndLoadProjectWithModules();
        assertEquals("test", loadProject.getAppName());
    }

}
