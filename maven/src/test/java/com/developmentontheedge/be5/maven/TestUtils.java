package com.developmentontheedge.be5.maven;

import com.developmentontheedge.be5.metadata.model.*;
import com.developmentontheedge.be5.metadata.serialization.LoadContext;
import com.developmentontheedge.be5.metadata.serialization.ModuleLoader2;
import com.developmentontheedge.be5.metadata.serialization.Serialization;
import com.developmentontheedge.be5.metadata.sql.Rdbms;
import com.developmentontheedge.be5.metadata.util.TestProjectUtils;
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

    TestProjectUtils utils = new TestProjectUtils();
    Path path;
    Project project;

    @Before
    public void setUp() throws Exception
    {
        path = tmp.newFolder().toPath();
        project = utils.getProject("test");
        Entity entity = utils.createEntity( project, "entity", "ID" );
        utils.createScheme( entity );
        utils.createScript( project );
        //utils.createQuery( entity );
        //utils.createOperation( entity );

        Path modulePath = tmp.newFolder().toPath();
        Project moduleProject = createModule(project, "testModule", modulePath);
        Serialization.save( project, path );


        ArrayList<URL> urls = new ArrayList<>();
        urls.add(modulePath.resolve("project.yaml").toUri().toURL());
        urls.add(path.resolve("project.yaml").toUri().toURL());
        ModuleLoader2.loadAllProjects(urls);


        LoadContext ctx = new LoadContext();
        ModuleLoader2.mergeAllModules( project, Collections.singletonList( moduleProject ), ctx );
    }

    private Project createModule(Project project, String moduleName, Path path) throws Exception
    {
        Project module = new Project( moduleName, true);
        Entity entity = utils.createEntity( module, "moduleEntity", "ID" );
        utils.createScheme( entity );
        utils.createScript( module );
        Serialization.save( module, path );

        Module appModule = new Module( moduleName, project.getModules() );
        project.setRoles( Arrays.asList( "Administrator", "Guest" ) );
        DataElementUtils.save( appModule );

        return module;
    }

    void createTestDB() throws Exception
    {
        initH2Connection(project);

        AppDb appDb = new AppDb();
        appDb.setBe5Project(project);
        appDb.execute();

        assertEquals(2, appDb.getCreatedTables());
        assertEquals(0, appDb.getCreatedViews());

        appDb.connector.executeInsert("INSERT INTO entity (name) VALUES ('bar')");
    }

    void initH2Connection(Project project)
    {
        String profileForIntegrationTests = "profileTestMavenPlugin";
        BeConnectionProfile profile = new BeConnectionProfile(profileForIntegrationTests, project.getConnectionProfiles().getLocalProfiles());
        profile.setConnectionUrl("jdbc:h2:~/"+ profileForIntegrationTests);
        profile.setUsername("sa");
        profile.setPassword("");
        profile.setDriverDefinition(Rdbms.H2.getDriverDefinition());
        DataElementUtils.save(profile);
        project.setConnectionProfileName(profileForIntegrationTests);

        project.setDatabaseSystem(Rdbms.H2);
    }

}
