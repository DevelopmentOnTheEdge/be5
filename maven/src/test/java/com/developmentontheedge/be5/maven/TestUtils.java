package com.developmentontheedge.be5.maven;

import com.developmentontheedge.be5.metadata.model.*;
import com.developmentontheedge.be5.metadata.serialization.LoadContext;
import com.developmentontheedge.be5.metadata.serialization.ModuleLoader2;
import com.developmentontheedge.be5.metadata.serialization.Serialization;
import com.developmentontheedge.be5.metadata.util.ProjectTestUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;

public abstract class TestUtils
{
    @Rule
    public TemporaryFolder tmp = new TemporaryFolder();

    Path tpmProjectPath;
    Project project;

    final String profileTestMavenPlugin = "profileTestMavenPlugin";

    @Before
    public void setUp() throws Exception
    {
        tpmProjectPath = tmp.newFolder().toPath();
        project = ProjectTestUtils.getProject("test");
        Entity entity = ProjectTestUtils.createEntity( project, "entity", "ID" );
        ProjectTestUtils.createScheme( entity );
        ProjectTestUtils.createScript( project, "delete from entity;\nINSERT INTO entity (name) VALUES ('foo')" );
        ProjectTestUtils.createH2Profile(project, "profileTestMavenPlugin");

        ProjectTestUtils.createQuery( entity );
        ProjectTestUtils.createOperation( entity );

        Path modulePath = tmp.newFolder().toPath();
        Project moduleProject = createModule(project, "testModule", modulePath);
        Serialization.save( project, tpmProjectPath);


        ArrayList<URL> urls = new ArrayList<>();
        urls.add(modulePath.resolve("project.yaml").toUri().toURL());
        urls.add(tpmProjectPath.resolve("project.yaml").toUri().toURL());
        ModuleLoader2.loadAllProjects(urls);


        LoadContext ctx = new LoadContext();
        ModuleLoader2.mergeAllModules( project, Collections.singletonList( moduleProject ), ctx );
    }

    private Project createModule(Project project, String moduleName, Path path) throws Exception
    {
        Project module = new Project( moduleName, true);
        Entity entity = ProjectTestUtils.createEntity( module, "moduleEntity", "ID" );
        ProjectTestUtils.createScheme( entity );
        ProjectTestUtils.createScript( module, "delete from moduleEntity;\nINSERT INTO moduleEntity (name) VALUES ('foo')" );
        Serialization.save( module, path );

        Module appModule = new Module( moduleName, project.getModules() );
        project.setRoles( Arrays.asList( "Administrator", "Guest", "User", "Operator" ) );
        DataElementUtils.save( appModule );

        return module;
    }

    void createTestDB() throws Exception
    {
        AppDb appDb = new AppDb();
        appDb.setBe5Project(project)
                .setProfileName(profileTestMavenPlugin)
                .execute();

        assertEquals(2, appDb.getCreatedTables());
        assertEquals(0, appDb.getCreatedViews());
    }

}
