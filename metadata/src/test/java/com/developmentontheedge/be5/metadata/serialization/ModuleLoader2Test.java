package com.developmentontheedge.be5.metadata.serialization;

import com.developmentontheedge.be5.metadata.exception.ProjectLoadException;
import com.developmentontheedge.be5.metadata.exception.ProjectSaveException;
import com.developmentontheedge.be5.metadata.model.ColumnDef;
import com.developmentontheedge.be5.metadata.model.DataElementUtils;
import com.developmentontheedge.be5.metadata.model.Entity;
import com.developmentontheedge.be5.metadata.model.EntityType;
import com.developmentontheedge.be5.metadata.model.Operation;
import com.developmentontheedge.be5.metadata.model.PageCustomization;
import com.developmentontheedge.be5.metadata.model.Project;
import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.metadata.model.SpecialRoleGroup;
import com.developmentontheedge.be5.metadata.model.TableDef;
import com.developmentontheedge.be5.metadata.sql.Rdbms;
import one.util.streamex.StreamEx;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class ModuleLoader2Test
{
    @Rule
    public TemporaryFolder tmp = new TemporaryFolder();

    @Test
    public void loadAllProjectsTest() throws IOException, ProjectSaveException, ProjectLoadException
    {
        Path path = tmp.newFolder().toPath();
        Project project = new Project( "test" );
        project.setRoles( Arrays.asList( "Administrator", "Guest", "User", "Operator" ) );
        project.setDatabaseSystem( Rdbms.POSTGRESQL );

        Serialization.save( project, path );

        ModuleLoader2.loadAllProjects(Collections.singletonList(path.resolve("project.yaml").toUri().toURL()));
    }

    @Test
    public void loadAllProjectsTestWithDev() throws IOException, ProjectSaveException, ProjectLoadException
    {
        Path path = tmp.newFolder().toPath();
        Project project = new Project( "test" );
        project.setRoles( Arrays.asList( "Administrator", "Guest", "User", "Operator" ) );
        project.setDatabaseSystem( Rdbms.POSTGRESQL );

        Serialization.save( project, path );

        try( PrintWriter out = new PrintWriter( path.resolve("dev.yaml").toFile() ) )
        {
            out.println( "paths:" + "\n    test: " + path.toAbsolutePath() );
        }

        ModuleLoader2.loadAllProjects(Collections.singletonList(path.resolve("project.yaml").toUri().toURL()));
        ModuleLoader2.readDevPathsToSourceProjects(Collections.singletonList(path.resolve("dev.yaml").toUri().toURL()));

        assertTrue(ModuleLoader2.getPathsToProjectsToHotReload().toString().startsWith("{test="));

        Project loadProject = ModuleLoader2.findAndLoadProjectWithModules();
        assertEquals("test", loadProject.getAppName());
    }

}
